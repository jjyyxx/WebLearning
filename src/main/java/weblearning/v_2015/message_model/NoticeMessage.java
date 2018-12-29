
package weblearning.v_2015.message_model;

@SuppressWarnings("unused")
public class NoticeMessage {
    public static class CourseNotice {
        public Long browseTimes;
        public String courseId;
        public String detail;
        public Object endDate;
        public Long id;
        public Long msgPriority;
        public String owner;
        public String regDate;
        public Object status;
        public String title;
    }

    public CourseNotice courseNotice;
    public String status;
}
