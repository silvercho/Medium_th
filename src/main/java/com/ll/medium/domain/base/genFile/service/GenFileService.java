package com.ll.medium.domain.base.genFile.service;

import com.ll.medium.domain.base.genFile.entity.GenFile;
import com.ll.medium.domain.base.genFile.repository.GenFileRepository;
import com.ll.medium.global.app.AppConfig;
import com.ll.medium.standard.util.Ut.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenFileService {
    private final GenFileRepository genFileRepository;

    // 조회
    public Optional<GenFile> findBy(String relTypeCode, Long relId, String typeCode, String type2Code, long fileNo) {
        return genFileRepository.findByRelTypeCodeAndRelIdAndTypeCodeAndType2CodeAndFileNo(relTypeCode, relId, typeCode, type2Code, fileNo);
    }

    @Transactional
    public GenFile save(String relTypeCode, long relId, String typeCode, String type2Code, long fileNo, MultipartFile sourceFile) {
        String sourceFilePath = Ut.file.toFile(sourceFile, AppConfig.getTempDirPath());
        return save(relTypeCode, relId, typeCode, type2Code, fileNo, sourceFilePath);
    }

    // 명령
    @Transactional
    public GenFile save(String relTypeCode, long relId, String typeCode, String type2Code, long fileNo, String sourceFile) {
        if (!Ut.file.exists(sourceFile)) return null;

        // fileNo 가 0 이면, 이 파일은 로직상 무조건 새 파일이다.
        if (fileNo > 0) remove(relTypeCode, relId, typeCode, type2Code, fileNo);

        String originFileName = Ut.file.getOriginFileName(sourceFile);
        String fileExt = Ut.file.getExt(originFileName);
        String fileExtTypeCode = Ut.file.getFileExtTypeCodeFromFileExt(fileExt);
        String fileExtType2Code = Ut.file.getFileExtType2CodeFromFileExt(fileExt);
        long fileSize = new File(sourceFile).length();
        String fileDir = getCurrentDirName(relTypeCode);

        int maxTryCount = 3;

        GenFile genFile = null;

        for (int tryCount = 1; tryCount <= maxTryCount; tryCount++) {
            try {
                if (fileNo == 0) fileNo = genNextFileNo(relTypeCode, relId, typeCode, type2Code);

                genFile = GenFile.builder()
                        .relTypeCode(relTypeCode)
                        .relId(relId)
                        .typeCode(typeCode)
                        .type2Code(type2Code)
                        .fileExtTypeCode(fileExtTypeCode)
                        .fileExtType2Code(fileExtType2Code)
                        .originFileName(originFileName)
                        .fileSize(fileSize)
                        .fileNo(fileNo)
                        .fileExt(fileExt)
                        .fileDir(fileDir)
                        .build();

                genFileRepository.save(genFile);

                break;
            } catch (Exception ignored) {

            }
        }

        File file = new File(genFile.getFilePath());

        file.getParentFile().mkdirs();

        Ut.file.moveFile(sourceFile, file);

        return genFile;
    }

    private long genNextFileNo(String relTypeCode, long relId, String typeCode, String type2Code) {
        return genFileRepository
                .findTop1ByRelTypeCodeAndRelIdAndTypeCodeAndType2CodeOrderByFileNoDesc(relTypeCode, relId, typeCode, type2Code)
                .map(genFile -> genFile.getFileNo() + 1)
                .orElse(1L);
    }

    private String getCurrentDirName(String relTypeCode) {
        return relTypeCode + "/" + Ut.date.getCurrentDateFormatted("yyyy_MM_dd");
    }

    public Map<String, GenFile> findGenFilesMapKeyByFileNo(String relTypeCode, long relId, String typeCode, String type2Code) {
        List<GenFile> genFiles = genFileRepository.findByRelTypeCodeAndRelIdAndTypeCodeAndType2CodeOrderByFileNoAsc(relTypeCode, relId, typeCode, type2Code);

        return genFiles
                .stream()
                .collect(Collectors.toMap(
                        genFile -> String.valueOf(genFile.getFileNo()), // key
                        genFile -> genFile // value
                ));
    }

    public Optional<GenFile> findById(long id) {
        return genFileRepository.findById(id);
    }

    @Transactional
    public void remove(String relTypeCode, long relId, String typeCode, String type2Code, long fileNo) {
        findBy(relTypeCode, relId, typeCode, type2Code, fileNo).ifPresent(this::remove);
    }

    @Transactional
    public void remove(GenFile genFile) {
        Ut.file.remove(genFile.getFilePath());
        genFileRepository.delete(genFile);
        genFileRepository.flush();
    }

    public List<GenFile> findByRelId(String modelName, long relId) {
        return genFileRepository.findByRelTypeCodeAndRelId(modelName, relId);
    }

    @Transactional
    public GenFile saveFile(String relTypeCode, long relId, String typeCode, String type2Code, long fileNo, MultipartFile file) {
        return save(relTypeCode, relId, typeCode, type2Code, fileNo, file);
    }
}