package com.epam.cip2.demo.service;

import com.epam.cip2.demo.exceptions.CsvProcessException;
import com.epam.cip2.demo.exceptions.FileNotFoundException;
import com.epam.cip2.demo.exceptions.NoColumnNameFoundException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

import static com.epam.cip2.demo.constant.MessageConstants.*;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    public double totalSum(List<String[]> rows, int columnIndex) {
        return rows.stream().skip(1)
                .map(row -> row[columnIndex])
                .mapToDouble(Double::parseDouble)
                .sum();
    }

    public int getColumnIndex(List<String[]> rows, String columnName) {
        String[] headers = rows.get(0);
        for (int columnIndex = 0; columnIndex < headers.length; columnIndex++) {
            if (headers[columnIndex].equalsIgnoreCase(columnName)) {
                return columnIndex;
            }
        }
        throw new NoColumnNameFoundException(NO_SUCH_COLUMN_FOUND);
    }

    public List<String[]> getRows(MultipartFile file) {
        String fileName = Objects.requireNonNull(file.getOriginalFilename());

        checkFileUploaded(fileName);
        checkFileExtension(fileName);

        Path path = Paths.get(fileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            Reader reader = Files.newBufferedReader(path);
            CSVReader csvReader = new CSVReaderBuilder(reader).build();

            return csvReader.readAll();
        } catch (IOException e) {
            LOGGER.error(ERROR_PROCESSING_CSV);
            throw new CsvProcessException(ERROR_PROCESSING_CSV);
        }
    }

    private void checkFileUploaded(String fileName) {
        if (fileName.isEmpty()) {
            throw new FileNotFoundException(NO_FILE_UPLOAD);
        }
    }

    private void checkFileExtension(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        if (!extension.equalsIgnoreCase(CSV)) {
            throw new IllegalArgumentException(NOT_CSV_FILE);
        }
    }
}

