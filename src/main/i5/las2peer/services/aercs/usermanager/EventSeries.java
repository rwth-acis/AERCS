package i5.las2peer.services.aercs.usermanager;

public class EventSeries {
    private String Name;
    private String Abbreviation;
    private int ParticipantNo;
    private int EventNo;
    private int id;
    
    private String key;
    
    public EventSeries() {
        this.Name = new String();
        this.Abbreviation = new String();
        this.ParticipantNo = 0;
        
    }
    public EventSeries(int seriesid){
        this.id = seriesid;
        this.Name = new String();
        this.Abbreviation = new String();
        this.ParticipantNo = 0;
    }
    public EventSeries(int seriesid, String sname, String sAbb, int seventno){
        this.id = seriesid;
        this.Name = sname;
        this.Abbreviation = sAbb;
        this.ParticipantNo = 0;
        this.setEventNo(seventno);
    }

    /**
     * @param seriesid
     * @param sname
     * @param sAbb
     * @param seventno
     * @param key
     * @updated 20.09.2009 by Quan Tran
     */
    public EventSeries(int seriesid, String sname, String sAbb, int seventno, String key)
    {
        this.id = seriesid;
        this.Name = sname;
        this.Abbreviation = sAbb;
        this.ParticipantNo = 0;
        this.setEventNo(seventno);
        this.key = key;
    }


    /**
     * @return
     * @updated 20.09.2009 by Quan Tran
     */
    public String getKey()
    {
        return this.key;
    }

    /**
     * @param key
     * @updated 20.09.2009 by Quan Tran
     */
    public void setKey(String key)
    {
        this.key = key;
    }


    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getAbbreviation() {
        return Abbreviation;
    }

    public void setAbbreviation(String Abbreviation) {
        this.Abbreviation = Abbreviation;
    }

    public int getParticipantNo() {
        return ParticipantNo;
    }

    public void setParticipantNo(int ParticipantNo) {
        this.ParticipantNo = ParticipantNo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventNo() {
        return EventNo;
    }

    public void setEventNo(int EventNo) {
        this.EventNo = EventNo;
    }
}
