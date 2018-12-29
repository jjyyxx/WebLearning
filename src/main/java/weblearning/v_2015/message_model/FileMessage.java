package weblearning.v_2015.message_model;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class FileMessage {
    public static class FileContainer {
        public List<BaseMessage.CourseCourseware> courseCoursewareList;
        public BaseMessage.CourseOutlines courseOutlines;
        public Long position;
        public Teacher teacherInfoView;
    }

    public static class Teacher {
        public String address;
        public String departmentId;
        public String email;
        public String gender;
        public String id;
        public String name;
        public String note;
        public String phone;
        public Object position;
        public String status;
        public String title;
        public Object zipCode;
    }

    long nodeId;
    String nodeName;
    int position;
    Teacher teacherInfoView;
    Map<String, FileContainer> childMapData;
}

// Map<String, FileMessage>