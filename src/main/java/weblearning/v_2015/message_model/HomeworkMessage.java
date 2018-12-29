package weblearning.v_2015.message_model;

@SuppressWarnings("unused")
public class HomeworkMessage {
    public static class CourseHomeworkRecord {
        public Object gradeUser;
        public Long groupId;
        public String groupName;
        public Object homewkDetail;
        public String homewkId;
        public String ifDelay;
        public Object iffine;
        public Object mark;
        public Object regDate;
        public Object replyDate;
        public Object replyDetail;
        public Object resourcesMappingByHomewkAffix;
        public Object resourcesMappingByReplyAffix;
        public Long seqId;
        public String status;
        public String studentId;
        public String teacherId;
    }

    public static class CourseHomeworkInfo {
        public Object answerDate;
        public Object answerDetail;
        public Object answerLink;
        public Object answerLinkFilename;
        public Long beginDate;
        public String courseId;
        public Object courseKnowledge;
        public String courseSource;
        public Object detail;
        public Long endDate;
        public Object homewkAffix;
        public Object homewkAffixFilename;
        public Object homewkGroupNum;
        public Long homewkId;
        public Object homewkIndex;
        public Long jiaoed;
        public Long noteId;
        public Long regDate;
        public Long teachingWeekId;
        public String title;
        public Long weiJiao;
        public Long yiJiao;
        public Long yiPi;
        public Long yiYue;
    }

    private CourseHomeworkInfo courseHomeworkInfo;
    private CourseHomeworkRecord courseHomeworkRecord;
}
