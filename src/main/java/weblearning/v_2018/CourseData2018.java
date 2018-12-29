package weblearning.v_2018;

import weblearning.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 仅提供基本支持，暂未进一步实现
 */
public class CourseData2018 extends CourseData {
    public CourseData2018(String href, String name, String s, String s1, String s2) {
        super(name, Version.V_2018);
    }

    @Override public CompletableFuture<Bulletin[]> resolveBulletins() {
        return CompletableFuture.completedFuture(new Bulletin[]{});
    }

    @Override public CompletableFuture<Information> resolveInformation() {
        return null;
    }

    @Override public CompletableFuture<Map<String, FileEntry[]>> resolveFileEntries() {
        return CompletableFuture.completedFuture(new HashMap<>());
    }

    @Override public CompletableFuture<Resource[]> resolveResources() {
        return null;
    }

    @Override public CompletableFuture<Operation[]> resolveOperations() {
        return CompletableFuture.completedFuture(new Operation[]{});
    }

    @Override public CompletableFuture<Map<String, String>> resolveAllOperationScores() {
        return null;
    }

    @Override public String getUrl() {
        return null;
    }
}
