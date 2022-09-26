package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;

import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.valueobjects.MemberInfo;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MemberInfoExtractor {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    private final String dateObservedValueJsonPath;
    private final String idJsonPath;

    public MemberInfoExtractor(String dateObservedValueJsonPath, String idJsonPath) {
        this.dateObservedValueJsonPath = dateObservedValueJsonPath;
        this.idJsonPath = idJsonPath;
    }

    public MemberInfo extractMemberInfo(String jsonString) {
        String dateObserved;
        try {
            dateObserved = JsonPath.read(jsonString, dateObservedValueJsonPath);
        } catch (
                PathNotFoundException pathNotFoundException) {
            dateObserved = LocalDateTime.now().format(formatter);
        }
        String id = JsonPath.read(jsonString, idJsonPath);
        return new MemberInfo(id, dateObserved);
    }
}
