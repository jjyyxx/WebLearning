package weblearning.v_2015;

import weblearning.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 仅提供基本支持，暂未进一步实现
 */
public class CourseData2015 extends CourseData {
    public CourseData2015(String url, String name, String operations, String notices, String files) {
        super(name, Version.V_2015);
        id = url.substring(54);
        this.unsubmittedOperations.set(Integer.valueOf(operations));
        this.unreadBulletins.set(Integer.valueOf(notices));
        this.unreadFiles.set(Integer.valueOf(files));
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
        return "http://learn.cic.tsinghua.edu.cn/f/student/coursehome/" + id;
    }
}
