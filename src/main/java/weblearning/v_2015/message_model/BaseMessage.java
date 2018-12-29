package weblearning.v_2015.message_model;

import java.util.List;

@SuppressWarnings("unused")
public class BaseMessage<T> {
    public static class Pagination<U> {
        public int currentPage;
        public String currentPageStr;
        public int pageMax;
        public int pageSize;
        public int pageStart;
        public int recordCount;
        public String recordCountStr;
        public List<U> recordList;
    }

    public String message;
    public T resultList;
    public Pagination<T> paginationList;

    public static class CourseCourseware {
        public String categoryId;
        public String courseId;
        public String courseKnowledge;
        public String courseSource;
        public String courseTeachingWeek;
        public String detail;
        public long id;
        public long ifOriginal;
        public long noteId;
        public String originalTeacher;
        public long position;
        public String regUser;
        public ResourcesMappingByFileId resourcesMappingByFileId;
        public String resourcesType;
        public String title;
    }

    public static class ResourcesMappingByFileId {
        public long browseNum;
        public String courseId;
        public long diskId;
        public long downloadNum;
        public String downloadUrl;
        public String extension;
        public String fileId;
        public String fileName;
        public String fileSize;
        public String playUrl;
        public long regDate;
        public String resourcesId;
        public long resourcesStatus;
        public String userCode;
    }

    public static class CourseOutlines {
        public String courseId;
        public String courseSource;
        public String descr;
        public String lastNodeName;
        public Object noteHierarchy;
        public long noteId;
        public String noteName;
        public long parentNote;
        public Object period;
        public String teacherId;
        public String teachingType;
        public Object teachingWeekId;
        public String title;
    }
}
