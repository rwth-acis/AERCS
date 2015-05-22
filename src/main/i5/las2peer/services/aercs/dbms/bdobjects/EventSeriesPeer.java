package i5.las2peer.services.aercs.dbms.bdobjects;


import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;


/**
 * @author Quan Tran
 */
public class EventSeriesPeer extends BasePeer
{

    public EventSeriesPeer()
    {
    }
    

    public ResultSet selectConferences(String startChar)
    {
        String query = "select id, name, abbreviation, series_key from eventseries " +
            "where name like '" + startChar + "%' and series_key like 'db/conf%' order by name";

        return this.executeQuery(query);
    }
    
    public ResultSet seriesAnalysisData(String dynamicQuery)
    {
        String query = dynamicQuery;

        return this.executeQuery(query);
    }

    public ResultSet selectJournals(String startChar)
    {
        String query = "select id, name, abbreviation, series_key from eventseries " +
            "where name like '" + startChar + "%' and series_key like 'db/journal%' order by name";

        return this.executeQuery(query);
    }
    
    public String getIdFromKey(String key)
    {
        String ret = null;
        String query = "select id from eventseries where series_key = '" + key + "'";
        
        ResultSet rs = this.executeQuery(query);

        try
        {
            if (rs.next())
            {
                ret = rs.getString(1);
            }

            rs.getStatement().close();
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    public ResultSet selectEventsForASeries(String id)
    {
        EventPeer ep = new EventPeer();
        return ep.selectEventsForASeries(id);
    }

    public ResultSet getDomains()
    {
        String query = "select id, name from domain";
        return executeQuery(query);
    }
    
    private String rankingsQuery(int sortCol, int sortOrder, int domainId, int type)
    {
        String order = null;
        switch (sortCol)
        {
            case 1: order = "betweenness"; break;
            case 2: order = "pagerank"; break;
            case 3: order = "authority"; break;
            case 4: order = "hub"; break;
            case 5: order = "evts.name"; break;
            default: order = "evts.name";
        }

        if (sortCol <= 4 && sortCol >= 1)
        {
            if (sortOrder == 0)
                order += " desc";
        }
        else
        {
            if (sortOrder > 0)
                order += " desc";
        }
        
        String query = 
            "select es.id, es.name, es.abbreviation, es.series_key, r.bn, r.pr, r.au, r.hu from " + 
            "(select s_id id, " + 
            "rank() over (order by betweenness desc) bn, " + 
            "rank() over (order by pagerank desc) pr, " + 
            "rank() over (order by authority desc) au, " + 
            "rank() over (order by hub desc) hu, ";
        
        if (order != null)
        {
            query += "row_number() over (order by " + order + ") rn ";
        }
        else
        {
            query += "row_number() over (order by evts.name) rn ";
            //query += "rownum rn ";
        }
        
        //query += "from citeseer.KNOWLEDGE50COUNT_CLUSTER " + 
         query += "from series_rank " + 
            "inner join eventseries evts " +
            "on s_id = evts.id ";

        if (domainId > 0)
        {
            query += "inner join series_domain sd " +
            "on s_id = sd.series_id " +
            "and sd.domain_id = " + domainId + " ";
        }

        if (type == 1)
        {
            query += "where evts.series_key like 'db/conf%' ";
        }
        
        if (type == 2)
        {
            query += "where evts.series_key like 'db/journal%' ";
        }
        
        query += ") r ";
        
        query += "inner join eventseries es " + 
            "on r.id = es.id ";

        return query;
    }
    
    public ResultSet getRankings(int firstRow, int lastRow, int sortCol, int sortOrder, int domainId, int type)
    {
        String query = rankingsQuery(sortCol, sortOrder, domainId, type);
        query += "where rn between " + firstRow + " and " + lastRow + " " + 
            "order by rn";
            
        ////////System.out.println(query);
        
        return this.executeQuery(query);
    }
    
    public int countRankings(int sortCol, int sortOrder, int domainId, int type)
    {
        int ret = 0;

        try
        {
            String query = rankingsQuery(sortCol, sortOrder, domainId, type);
            ResultSet rs = executeQuery("select count(*) from (" + query + ")");

            if (rs.next())
            {
                ret = rs.getInt(1);
            }
            
            rs.getStatement().close();
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return ret;
    }
    
    /**
     * @param seriesID
     * @return
     * @updated 13.05.2012 by Rizwan Uppal
     */
    public ResultSet getSeriesInfo(String seriesID)
    {
        String query = "select id, name, abbreviation, series_key from eventseries " +
            "where id='" + seriesID + "'";

        return this.executeQuery(query);
    }
    
    /**
     * @param seriesID
     * @return
     * @updated 13.05.2012 by Rizwan Uppal
     */
    public ResultSet searchSeries(String searchKeyword, String typeOfSeriesSearchIn)
    {
        String criteria ="";
        if(typeOfSeriesSearchIn.equals("both"))
            criteria = "(LOWER(name) like '%" + searchKeyword.toLowerCase() + "%' or LOWER(abbreviation) like '%" + searchKeyword.toLowerCase() + "%' ) and (series_key like 'db/conf%' or series_key like 'db/journal%')";
        
        if(typeOfSeriesSearchIn.equals("conferences"))
            criteria = "(LOWER(name) like '%" + searchKeyword.toLowerCase() + "%' or LOWER(abbreviation) like '%" + searchKeyword.toLowerCase() + "%' ) and series_key like 'db/conf%'";
            
        if(typeOfSeriesSearchIn.equals("journals"))
            criteria = "(LOWER(name) like '%" + searchKeyword.toLowerCase() + "%' or LOWER(abbreviation) like '%" + searchKeyword.toLowerCase() + "%' ) and series_key like 'db/journal%'";
            
        String query = "select distinct id, name, abbreviation, series_key from eventseries " +
            "where "+criteria+" order by name";

        return this.executeQuery(query);
    }
    
    /**
     * @param seriesID
     * @return
     * @updated 13.05.2012 by Rizwan Uppal
     */
    public ResultSet seriesComparisonData(String[][] selectedSeries, String tableName, String[] chartParameters, String yearColumnName)
    {    
      String queryPart1 = "distinct(so."+ yearColumnName +"), \n";
      String queryPart2 = "( ";
      
      for(int i=0 ; i<chartParameters.length ; i++)
      {
        for(int j=0 ; j<selectedSeries.length ; j++)
        {
          queryPart1 = queryPart1 + "round((select distinct(a." + chartParameters[i] + ") from "+ tableName +" a where a."+ yearColumnName +"=so."+ yearColumnName +" and a.SERIES_ID = " + selectedSeries[j][0] + "),3) as " + chartParameters[i];
          //queryPart1 = queryPart1 + "(select distinct(a." + chartParameters[i] + ") from "+ tableName +" a where a.AGE=so.AGE and a.SERIES_ID = " + selectedSeries[j][0] + ") as " + chartParameters[i];
                   
          if( !(i+1==chartParameters.length && j+1==selectedSeries.length) )
          {
            queryPart1 = queryPart1 + ", \n";
          }
          else
          {
            queryPart1 = queryPart1 + " ";
          }
          
          if(i==0)
          {
            queryPart2 = queryPart2 + "so.SERIES_ID="+selectedSeries[j][0];
            
            if( selectedSeries.length==1 || j+1==selectedSeries.length )
            {
                queryPart2 = queryPart2 + " ) ";
            }
            else
            {
                queryPart2 = queryPart2 + " or ";
            }
          }
        }
      }
      String query = "select \n"+queryPart1+"\nfrom \n"+ tableName +" so \nwhere\n"+""+queryPart2+"\norder by so."+ yearColumnName +"";
      //////System.out.println(query);
      return this.executeQuery(query);
    }
    
    /**
     * @param seriesID
     * @return
     * @updated 13.05.2012 by Rizwan Uppal
     */
    public String[] createGoogleURL(String[][] selectedSeries, String chartType, String[] color, boolean adjustGraphAxisLimits) throws SQLException
    {
        String chartName[] = new String[1];;
        String columnNames[] = new String[1];
        String yearColumnName = "";
        String tableName = "";
        
        if(chartType.equals("a") || chartType.equals("c"))
        {
            int noOfColumns = 7;
            if(chartType.equals("a"))
                tableName = "SERIES_AUTHORSHIP_TIMESERIES";
            if(chartType.equals("c"))
                tableName = "SERIES_CITATION_TIMESERIES";
            
            chartName = new String[noOfColumns+1];
            chartName[0] = "Edges / Nodes Over Year";
            chartName[1] = "Nodes / Year";
            chartName[2] = "Edges / Year";                               
            chartName[3] = "Clustering / Year";
            chartName[4] = "Maximum Betweenness / Year";
            chartName[5] = "Largest Component / Year";
            chartName[6] = "Diameter / Year";
            chartName[7] = "Average Path Length / Year";
            
            columnNames = new String[noOfColumns];
            columnNames[0] = "VCOUNT";
            columnNames[1] = "ECOUNT";
            columnNames[2] = "CLUSTERING";
            columnNames[3] = "BETWEENNESS";
            columnNames[4] = "LARGEST_COMPONENT";
            columnNames[5] = "DIAMETER";
            columnNames[6] = "AVERAGE_PATH_LENGTH";
            
            yearColumnName = "AGE";
        }

        if(chartType.equals("r"))
        {
            int noOfColumns = 2;
            
            tableName = "SERIES_RECURRING_AUTHOR";
            
            chartName = new String[noOfColumns+1];
            chartName[1] = "Recurring Authors / Year";
            chartName[2] = "Papers by Recurring Authors / Year";
            
            columnNames = new String[noOfColumns];
            columnNames[0] = "RECURRING_AUTHOR";
            columnNames[1] = "RECURRING_PAPER";
            
            yearColumnName = "YEAR";
        }
        
        String url[] = new String[chartName.length];
        String dataSet[][] = new String[0][0];
        int NoOfRecords=0;
            
        ResultSet rs = seriesComparisonData(selectedSeries, tableName, columnNames, yearColumnName);
        if(rs!=null)
        {
           // try{
                rs.last();
                NoOfRecords = rs.getRow();
                rs.beforeFirst();
                
                ////////System.out.println("No of Records : "+NoOfRecords);  
                
                dataSet = new String[NoOfRecords][(selectedSeries.length*(chartName.length-1))+1];
              
                int i = 0;
                while(rs.next())
                {
                    for(int j=0;j<((selectedSeries.length*(chartName.length-1))+1);j++)
                    {
                        String tmp = rs.getString(j+1);
                        
                        if(tmp==null)
                        {
                            dataSet[i][j] = "null";
                            ////////System.out.println("Riz "+tmp);
                        }
                        else
                        {
                            dataSet[i][j] = tmp;
                        }
                    }
                    i++;
                }
                rs.close();
                
            String startURL = "\nhttp://chart.apis.google.com/chart\n?chs=450x450\n&cht=lxy";
            String chma = "\n&chma=0,0,10,40";
            //String chdlp = "\n&chdlp=r";
            String chg = "\n&chg=10,10";
            String chxt = "\n&chxt=x,y,r";
            String chtt = "\n&chtt=";
            String  chco="\n&chco=", chls="\n&chls=", chds="\n&chds=\n", chd="\n&chd=t:\n", chdl="\n&chdl=", chm="\n&chm=\n", chxr="\n&chxr=\n";
            
            String axisX = "";
            String axisY[][] = new String[chartName.length-1][selectedSeries.length];
            int xAxisMax = 0;
            Double  yAxisMin[] = new Double[chartName.length];
            for(int k=0;k<yAxisMin.length;k++)
            {
                yAxisMin[k] = 0.0;
            }
            Double  yAxisMax[] = new Double[chartName.length];
            for(int k=0;k<yAxisMax.length;k++)
            {
                yAxisMax[k] = 0.0;
            }
                
            boolean continueLoop = true;
            for(int x=0;x<dataSet.length;x++)
            {
                for(int y=0;y<dataSet[0].length;y++)
                {
                    if(y==0)
                    {
                        if(dataSet.length==1 || x+1==dataSet.length)
                        {
                            axisX = axisX + dataSet[x][y];
                            xAxisMax  = 1+ Integer.parseInt(dataSet[x][y]);
                            ////////////System.out.println("===>"+xAxisMax);
                        }
                        else
                        {
                            axisX = axisX + dataSet[x][y] + ",";
                        }
                        
                        //xAxisMax = Integer.parseInt(dataSet[x][y]);
                        ////////System.out.print(dataSet[x][y]+"\t\t");
                    }
                        
                    ///////////////////////////////////////////0///////////////////////////////////////
                        
                    for(int z=0; z<chartName.length-1;z++)
                    {
                        if(y>=(selectedSeries.length*z)+1 && y<=(selectedSeries.length*z)+selectedSeries.length)
                        {
                            if(x==0)
                            {
                                axisY[z][y-((selectedSeries.length*z)+1)] = "";  
                                
                                if(continueLoop)
                                {
                                    if((y==(selectedSeries.length*z) || y==(selectedSeries.length*z)+selectedSeries.length))
                                    {
                                        chco = chco + color[y-((selectedSeries.length*z)+1)];
                                        chls = chls + "2";
                                        chdl = chdl + selectedSeries[y-((selectedSeries.length*z)+1)][1];
                                        chm = chm + "o,"+ color[y-((selectedSeries.length*z)+1)] +","+ (y-((selectedSeries.length*z)+1)) +",-1,5";
                                        continueLoop =false;
                                    }
                                    else
                                    {
                                        chco = chco + color[y-((selectedSeries.length*z)+1)] + ",";
                                        chls = chls + "2|";
                                        chdl = chdl + selectedSeries[y-((selectedSeries.length*z)+1)][1] + "|";
                                        chm = chm + "o,"+ color[y-((selectedSeries.length*z)+1)] +","+ (y-((selectedSeries.length*z)+1)) +",-1,5|\n";
                                    }
                                }
                            }
                            if(dataSet.length==1 || x+1==dataSet.length)
                            {
                                axisY[z][y-((selectedSeries.length*z)+1)] = axisY[z][y-((selectedSeries.length*z)+1)] + dataSet[x][y];
                            }
                            else
                            {
                                axisY[z][y-((selectedSeries.length*z)+1)] = axisY[z][y-((selectedSeries.length*z)+1)] + dataSet[x][y] + ",";
                            }
                        
                            if(!dataSet[x][y].equals("null"))
                            {
                                if( Double.parseDouble(dataSet[x][y]) > yAxisMax[z] )
                                    yAxisMax[z] = Double.parseDouble(dataSet[x][y]);
                            
                                if( Double.parseDouble(dataSet[x][y]) < yAxisMin[z] )
                                    yAxisMin[z] = Double.parseDouble(dataSet[x][y]);
                            }
                            ////////System.out.print(dataSet[x][y]+"\t\t");
                        }
                    }
                    //////////////////////////////////////////////////////////////////////////////////                
                }
                //////System.out.print("\n");
            }
            //////////System.out.println(axisX);
            /*for(int x=0;x<chartName.length-1;x++)
            {
                for(int j=0 ; j<axisY[0].length; j++)
                {
                        //////////System.out.println(axisY[x][j]);
                }
            }*/
            //////////System.out.println(chco);
            //////////System.out.println(chls);
            //////////System.out.println(chds);
            //////////System.out.println(chdl);
            //////////System.out.println(chm);
            //////////System.out.println(chxr);
                
            for(int k=0;k<chartName.length-1;k++)
            {
                if(adjustGraphAxisLimits)
                    chxr = chxr + "0,0,"+ xAxisMax +"|\n1,"+ round3Decimals(yAxisMin[k]-((yAxisMin[k]+yAxisMax[k])/NoOfRecords)) +","+ round3Decimals(yAxisMax[k]+((yAxisMin[k]+yAxisMax[k])/NoOfRecords)) +"|\n2,"+ round3Decimals(yAxisMin[k]-((yAxisMin[k]+yAxisMax[k])/NoOfRecords)) +","+ round3Decimals(yAxisMax[k]+((yAxisMin[k]+yAxisMax[k])/NoOfRecords)) ;
                else
                    chxr = chxr + "0,0,"+ xAxisMax +"|\n1,"+ round3Decimals(yAxisMin[k]-((yAxisMin[k]+yAxisMax[k])/NoOfRecords)) +","+ round3Decimals(yAxisMax[k]) +"|\n2,"+ round3Decimals(yAxisMin[k]) +","+ round3Decimals(yAxisMax[k]) ;
              
                for(int j=1; j<=selectedSeries.length; j++)
                {
                    if(selectedSeries.length==1 || selectedSeries.length==j)
                    {
                        if(adjustGraphAxisLimits)
                            chds = chds + "0,"+ (xAxisMax) +","+ round3Decimals(yAxisMin[k]-((yAxisMin[k]+yAxisMax[k])/NoOfRecords))  +","+ round3Decimals(yAxisMax[k]+((yAxisMin[k]+yAxisMax[k])/NoOfRecords));
                        else
                            chds = chds + "0,"+ (xAxisMax) +","+ round3Decimals(yAxisMin[k])  +","+ round3Decimals(yAxisMax[k]);
                    }
                    else
                    {
                        if(adjustGraphAxisLimits)
                            chds = chds + "0,"+ (xAxisMax) +","+ round3Decimals(yAxisMin[k]-((yAxisMin[k]+yAxisMax[k])/NoOfRecords))  +","+ round3Decimals(yAxisMax[k]+((yAxisMin[k]+yAxisMax[k])/NoOfRecords)) +",\n";  
                        else
                            chds = chds + "0,"+ (xAxisMax) +","+ round3Decimals(yAxisMin[k])  +","+ round3Decimals(yAxisMax[k]) +",\n";  
                    }
                }
              
                for(int j=0 ; j<axisY[k].length; j++)
                {
                    String tmp_xAxis="", tmp_yAxis="";
                        
                    if(!(axisY[k][j]==null))
                    {    
                        StringTokenizer st_x = new StringTokenizer(axisX, ","); 
                        StringTokenizer st_y = new StringTokenizer(axisY[k][j], ","); 
                    
                        //////System.out.println(axisX);
                        //////System.out.println(axisY[k][j]);
                    
                    
                        int count = 0;
                        while(st_x.hasMoreTokens() && st_y.hasMoreTokens()) 
                        { 
                            String x_element = st_x.nextToken(); 
                            String y_element = st_y.nextToken(); 
                            if(!(x_element.equals("null") || y_element.equals("null"))) 
                            {
                                if(count==0)
                                {
                                    tmp_xAxis += x_element;
                                    tmp_yAxis += y_element;
                                }
                                else
                                {
                                    tmp_xAxis += "," + x_element;
                                    tmp_yAxis += "," + y_element;
                                }
                            }
                            count++;
                        } 
                    
                        //////System.out.println(tmp_xAxis);
                        //System.out.print(tmp_yAxis);
                    
                        if(tmp_xAxis.equals(""))
                        {
                            tmp_xAxis = "0";
                        }
                        if(tmp_yAxis.equals(""))
                        {
                            tmp_yAxis = "0";
                        }
                        
                        if(axisY[k].length==1 || j+1==axisY[k].length)
                            chd = chd + tmp_xAxis + "|" + tmp_yAxis;
                        else
                            chd = chd + tmp_xAxis + "|" + tmp_yAxis + "|\n"; 
                    
                        //System.out.print(chd);
                        //////System.out.println();
                    }
                }
                chtt = chtt + chartName[k+1];
                    
                url[k+1] = startURL+chtt+chco+chls+chma+chds+chd+chm+chg+chxt+chxr;
            
                //while(url[k+1].indexOf("||")!=-1)
                 //   url[k+1] = url[k+1].substring (0, url[k+1].indexOf("||"))+"|0|"+url[k+1].substring (url[k+1].indexOf("||")+2, url[k+1].length()-1);
                ////////System.out.println(url[k+1]);
                    
                chxr = "\n&chxr=\n";
                chds = "\n&chds=\n";
                chtt = "\n&chtt=";
                chd = "\n&chd=t:\n";
               // }
            }
            
            if(columnNames.length==7)
            {
                chds = "\n&chds=\n";
                chtt = "\n&chtt=" + chartName[0];
                chd = "\n&chd=t:\n";
    
                double xMin=0.0, xMax=0.0, yMin=0.0, yMax=0.0;
                for(i=1;i<=selectedSeries.length;i++)
                {    
                    StringTokenizer st1 = new StringTokenizer(axisY[0][i-1], ",");  
                    StringTokenizer st2 = new StringTokenizer(axisY[1][i-1], ",");
                    String tmp_xAxis="", tmp_yAxis="";
                    int count=0;
                    while(st1.hasMoreTokens() && st2.hasMoreTokens()) 
                    {
                        String x_value = st1.nextToken();
                        String y_value = st2.nextToken();
                            
                        if(!(x_value.equals("null") || y_value.equals("null"))) 
                        {
                            if(count==0)
                            {
                                tmp_xAxis += x_value;
                                tmp_yAxis += y_value;
                            }
                            else
                            {
                                tmp_xAxis += "," + x_value;
                                tmp_yAxis += "," + y_value;
                            }
                                        
                            if(xMin>Double.parseDouble(x_value))
                                xMin = Double.parseDouble(x_value);
                            if(xMax<Double.parseDouble(x_value))
                                xMax = Double.parseDouble(x_value);
                            
                            if(yMin>Double.parseDouble(y_value))
                                yMin = Double.parseDouble(y_value);
                            if(yMax<Double.parseDouble(y_value))
                                yMax = Double.parseDouble(y_value);
                        }
                        count++;   
                    }  
                    if(dataSet.length==1 || i==selectedSeries.length)
                        chd += tmp_xAxis + "|" + tmp_yAxis;
                    else
                        chd += tmp_xAxis + "|" + tmp_yAxis + "|\n";
                }
                /*//////////System.out.println("x min"+xMin);
                //////////System.out.println("x max"+xMax);
                //////////System.out.println("y min"+yMin);
                //////////System.out.println("y max"+yMax);*/
                
                if(adjustGraphAxisLimits)    
                    chxr += "0,"+ round3Decimals(xMin-((xMin+xMax)/NoOfRecords)) +","+ round3Decimals(xMax+((xMin+xMax)/NoOfRecords)) +"|\n1,"+ round3Decimals(yMin-((yMin+yMax)/NoOfRecords)) +","+ round3Decimals(yMax+((yMin+yMax)/NoOfRecords)) +"|\n2,"+ round3Decimals(yMin-((yMin+yMax)/NoOfRecords)) +","+ round3Decimals(yMax+((yMin+yMax)/NoOfRecords));
                else
                    chxr += "0,"+ round3Decimals(xMin) +","+ round3Decimals(xMax) +"|\n1,"+ round3Decimals(yMin) +","+ round3Decimals(yMax) +"|\n2,"+ round3Decimals(yMin) +","+ round3Decimals(yMax) ;
                
                for(i=0; i<selectedSeries.length;i++)
                {
                    ////////System.out.println(round3Decimals(xMin-((xMin+xMax)/NoOfRecords)));
                    ////////System.out.println(round3Decimals(xMax+((xMin+xMax)/NoOfRecords)));
                    ////////System.out.println(round3Decimals(yMin-((yMin+yMax)/NoOfRecords)));
                    ////////System.out.println(round3Decimals(yMax+((yMin+yMax)/NoOfRecords)));
                    
                    if(dataSet.length==1 || i+1==selectedSeries.length)
                    {
                        if(adjustGraphAxisLimits)
                            chds += round3Decimals(xMin-((xMin+xMax)/NoOfRecords)) +","+ round3Decimals(xMax+((xMin+xMax)/NoOfRecords)) +","+ round3Decimals(yMin-((yMin+yMax)/NoOfRecords)) +","+ round3Decimals(yMax+((yMin+yMax)/NoOfRecords));
                        else
                            chds += round3Decimals(xMin) +","+ round3Decimals(xMax) +","+ round3Decimals(yMin) +","+ round3Decimals(yMax);
                    }
                    else
                    {
                        if(adjustGraphAxisLimits)
                            chds += round3Decimals(xMin-((xMin+xMax)/NoOfRecords)) +","+ round3Decimals(xMax+((xMin+xMax)/NoOfRecords)) +","+ round3Decimals(yMin-((yMin+yMax)/NoOfRecords)) +","+ round3Decimals(yMax+((yMin+yMax)/NoOfRecords))+",\n";
                        else
                            chds += round3Decimals(xMin) +","+ round3Decimals(xMax) +","+ round3Decimals(yMin) +","+ round3Decimals(yMax)+",\n";
                    }
                }
                //selectedSeries.length
                ////////////System.out.println("Riz : "+chd);
                    
                url[0] = startURL+chtt+chco+chls+chma+chds+chd+chm+chg+chxt+chxr;
                
                while(url[0].indexOf("||")!=-1)
                    url[0] = url[0].substring (0, url[0].indexOf("||"))+"|0|0"+url[0].substring (url[0].indexOf("||")+2, url[0].length()-1);
                //////System.out.println(url[0]);
            }
        }
        return url;
    }
    
    /**
     * @param seriesID
     * @return
     * @updated 13.05.2012 by Rizwan Uppal
     */
    public String createSingleGoogleChartURL(String[] color, String chartName, ResultSet rs, int columns, boolean adjustGraphAxisLimits) throws SQLException
    {  
        
        String url="";
        String dataSet[][] = new String[0][0];
        int NoOfRecords=0;
                        
            //EventSeriesPeer es = new EventSeriesPeer();   
            //ResultSet rs = es.GoogleGraphData(query);
        if(rs!=null)
        {
            rs.last();
            NoOfRecords = rs.getRow();
            rs.beforeFirst();
                                
            ////System.out.println("No of Records : "+NoOfRecords);  
                                
            dataSet = new String[NoOfRecords][columns];
                            
            int i = 0;
                
            while(rs.next())
            {
                for(int j=0;j<columns;j++)
                {
                    String tmp = rs.getString(j+1);
                                        
                    if(tmp==null)
                    {
                        dataSet[i][j] = "null";
                        ////////System.out.println("Riz "+tmp);
                    }
                    else
                    {
                        dataSet[i][j] = tmp;
                    }
                }
                i++;
            }
            rs.close();
                                    
            String startURL = "\nhttp://chart.apis.google.com/chart\n?chs=450x450\n&cht=lxy";
            String chma = "\n&chma=0,0,10,40";
            //String chdlp = "\n&chdlp=r";
            String chg = "\n&chg=10,10";
            String chxt = "\n&chxt=x,y,r";
            String chtt = "\n&chtt=";
            String  chco="\n&chco=", chls="\n&chls=", chds="\n&chds=\n", chd="\n&chd=t:\n", chdl="\n&chdl=", chm="\n&chm=\n", chxr="\n&chxr=\n";
                            
            String axisX = "";
            String axisY[] = new String[columns-1];
                             
            for(int x=0;x<columns-1; x++)
            {
                axisY[x] = "";
            }
                           
            Double xAxisMin = 9999999999999999999999999999.9;
            Double xAxisMax = -9999999999999999999999999999.9;
            Double  yAxisMin = 9999999999999999999999999999.9;
            Double  yAxisMax = -9999999999999999999999999999.9;
                            
           /* for(int a=0;a<dataSet.length;a++)
            {
                for(int b=0;b<dataSet[0].length;b++)
                {
                    ////System.out.println(dataSet[a][b]);
                }
            }*/
                    
            for(int row=0;row<dataSet.length;row++)
            {
                boolean executeOnce = true; 
                                
                for(int col=0;col<dataSet[row].length;col++)
                {
                    //////////////////////////////////////// For X Asis ///////////////////////////////////////
                    if(col==0)
                    {
                        if(dataSet.length==1 || row+1==dataSet.length)
                        {
                            axisX = axisX + dataSet[row][col];
                                            
                            ////////////System.out.println("===>"+xAxisMax);
                        }
                        else
                        {
                            axisX = axisX + dataSet[row][col] + ",";
                        }
                                        
                        double newXvalue = Double.parseDouble(dataSet[row][col]);
                        
                        if(newXvalue > xAxisMax)
                            xAxisMax  = newXvalue;
                        if(newXvalue < xAxisMin)
                            xAxisMin = newXvalue;
                                        
                        //xAxisMax = Integer.parseInt(dataSet[x][y]);
                        ////////System.out.print(dataSet[x][y]+"\t\t");
                    }
                    else
                    {
                    ///////////////////////////////////////////0///////////////////////////////////////
                        ////System.out.println("Row :"+row);
                        ////System.out.println("Col :"+col);
                        ////System.out.println("axisY[].length :"+axisY.length);
                                        
                        if(dataSet.length==1 || (row+1==dataSet.length))
                        {
                            ////System.out.println(0+"--------------------------------------------------------");
                            ////System.out.println("Row :"+row);
                            ////System.out.println("Col :"+col);
                            ////System.out.println("axisY[].length :"+axisY.length);
                            ////System.out.println("dataSet["+row+"]["+col+"] :"+dataSet[row][col]);
                            axisY[col-1] = axisY[col-1] + dataSet[row][col];
                            ////System.out.println(1);          
                        }
                        else
                        {
                            ////System.out.println(5);
                            axisY[col-1] = axisY[col-1] + dataSet[row][col] + ",";
                            ////System.out.println(6);
                        }
                        if(row==0)
                        {                      
                            if(dataSet[0].length==1 || (col+1==dataSet[0].length))
                            {
                                ////System.out.println(2);
                                chco = chco + color[col-1];
                                ////System.out.println(chco);
                                chls = chls + "2";
                                chm = chm + "o,"+ color[col-1] +","+ (col-1) +",-1,5";
                                ////System.out.println(chm);
                                ////System.out.println(3);
                            }
                            else
                            {
                                ////System.out.println(7);
                                chco = chco + color[col-1] + ",";
                                ////System.out.println(chco);
                                chls = chls + "2|";
                                chm = chm + "o,"+ color[col-1] +","+ (col-1) +",-1,5|\n";
                                ////System.out.println(chm);
                                ////System.out.println(8);
                            }
                        }
                        ////System.out.println(10);
                        if(!dataSet[row][col].equals("null"))
                        {
                            if( Double.parseDouble(dataSet[row][col]) > yAxisMax )
                                yAxisMax = Double.parseDouble(dataSet[row][col]);
                                    
                            if( Double.parseDouble(dataSet[row][col]) < yAxisMin )
                                yAxisMin = Double.parseDouble(dataSet[row][col]);
                        }      
                    }
                }
                //////System.out.print("\n");
            }
            xAxisMax++;
            xAxisMin--;
            ////System.out.println("Xaxis :"+axisX);
            ////System.out.println("xAxisMax :"+xAxisMax);
            ////System.out.println("yAxisMin :"+yAxisMin);
            ////System.out.println("yAxisMax :"+yAxisMax);
            /*for(int x=0;x<columns-1; x++){
                //System.out.println("axisY["+x+"] :"+axisY[x]);}
            */
            ////System.out.println(chco);
            ////System.out.println(chls);
            ////System.out.println(chds);
            ////System.out.println(chdl);
            ////System.out.println(chm);
            ////System.out.println(chxr);
            if(adjustGraphAxisLimits)                   
                chxr = chxr + "0,"+xAxisMin+","+ xAxisMax +"|\n1,"+ round3Decimals(yAxisMin-((yAxisMin+yAxisMax)/NoOfRecords)) +","+ round3Decimals(yAxisMax+((yAxisMin+yAxisMax)/NoOfRecords)) +"|\n2,"+ round3Decimals(yAxisMin-((yAxisMin+yAxisMax)/NoOfRecords)) +","+ round3Decimals(yAxisMax+((yAxisMin+yAxisMax)/NoOfRecords)) ;
            else
                chxr = chxr + "0,"+xAxisMin+","+ xAxisMax +"|\n1,"+ yAxisMin +","+ round3Decimals(yAxisMax) +"|\n2,"+ round3Decimals(yAxisMin) +","+ round3Decimals(yAxisMax) ;
                              
            for(int j=0; j<dataSet[0].length; j++)
            {
                if(dataSet[0].length==1 || (j+1==dataSet[0].length))
                {
                    if(adjustGraphAxisLimits) 
                        chds = chds + ""+xAxisMin+","+ (xAxisMax) +","+ round3Decimals(yAxisMin-((yAxisMin+yAxisMax)/NoOfRecords))  +","+ round3Decimals(yAxisMax+((yAxisMin+yAxisMax)/NoOfRecords));
                    else
                        chds = chds + ""+xAxisMin+","+ (xAxisMax) +","+ round3Decimals(yAxisMin)  +","+ round3Decimals(yAxisMax);
                }
                else
                {
                    if(adjustGraphAxisLimits)
                        chds = chds + ""+xAxisMin+","+ (xAxisMax) +","+ round3Decimals(yAxisMin-((yAxisMin+yAxisMax)/NoOfRecords))  +","+ round3Decimals(yAxisMax+((yAxisMin+yAxisMax)/NoOfRecords)) +",\n";  
                    else
                    chds = chds + ""+xAxisMin+","+ (xAxisMax) +","+ round3Decimals(yAxisMin)  +","+ round3Decimals(yAxisMax) +",\n";  
                }        
            }
                           
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            for(int j=0 ; j<axisY.length; j++)
            {
                String tmp_xAxis="", tmp_yAxis="";
                                        
                StringTokenizer st_x = new StringTokenizer(axisX, ","); 
                StringTokenizer st_y = new StringTokenizer(axisY[j], ","); 
                                    
                //////System.out.println(axisX);
                //////System.out.println(axisY[k][j]);
                                          
                int count = 0;
                                
                while(st_x.hasMoreTokens() && st_y.hasMoreTokens()) 
                { 
                    String x_element = st_x.nextToken(); 
                    String y_element = st_y.nextToken(); 
                                    
                    if(!(x_element.equals("null") || y_element.equals("null"))) 
                    {
                        if(count==0)
                        {
                            tmp_xAxis += x_element;
                            tmp_yAxis += y_element;
                        }
                        else
                        {
                            tmp_xAxis += "," + x_element;
                            tmp_yAxis += "," + y_element;
                        }
                    }
                    count++;
                }                   
                //////System.out.println(tmp_xAxis);
                //System.out.print(tmp_yAxis);
                                    
                if(tmp_xAxis.equals(""))
                {
                    tmp_xAxis = "0";
                }
                if(tmp_yAxis.equals(""))
                {
                    tmp_yAxis = "0";
                }
                                        
                if(axisY.length==1 || j+1==axisY.length)
                    chd = chd + tmp_xAxis + "|" + tmp_yAxis;
                else
                    chd = chd + tmp_xAxis + "|" + tmp_yAxis + "|\n"; 
                                    
                //System.out.print(chd);
                //////System.out.println();
            }
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        
            chtt = chtt + chartName;
                            
            ////System.out.println(chco);
            ////System.out.println(chls);
            ////System.out.println(chds);
            ////System.out.println(chdl);
            ////System.out.println(chm);
            ////System.out.println(chxr);
                                    
            url = startURL+chtt+chco+chls+chma+chds+chd+chm+chg+chxt+chxr;
                            
            while(url.indexOf("||")!=-1)
                url = url.substring (0, url.indexOf("||"))+"|0|"+url.substring (url.indexOf("||")+2, url.length()-1);
            ////////System.out.println(url[k+1]);             
        }
        ////System.out.println(url);
        return url;
    }
    
    /**
     * @param seriesID
     * @return
     * @updated 13.05.2012 by Rizwan Uppal
     */
    public ResultSet queryDevelopmentChart(String seriesID)
    {
        try
        {
            String query = "select ev.year, count(pa.author_id) " + 
            "from participate pa " + 
            "inner join proceeding_event pe on pa.proceeding_id = pe.proceeding_id " + 
            "inner join event ev on pe.event_id = ev.id " + 
            //"where pa.role = 1 and ev.series_id = " + seriesID + " "  +
            "where ev.series_id = " + seriesID + " "  +
            "group by ev.year " + 
            "order by ev.year";
            ////System.out.println(query);
            
            ResultSet rs = this.executeQuery(query);
            
            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
     /**
      * @param seriesID
      * @return
      * @updated 13.05.2012 by Rizwan Uppal
      */
    public ResultSet queryContinuityChart(String seriesID)
    {
        try
        {   
            //ResultSet rs = con.executeQuery("select count(id), time from (select count(a.event_id) time, a.scientist_id id from participate a, academicevent b where a.event_id = b.id and a.role=1 and  b.series_id = "+seriesID+" group by a.scientist_id) group by time order by time");
            String query = "select time, count(id) from " + 
            "(select count(pa.proceeding_id) time, pa.author_id id " + 
            "from participate pa " + 
            "inner join proceeding_event pe on pa.proceeding_id = pe.proceeding_id " + 
            "inner join event ev on pe.event_id = ev.id " + 
            //"where pa.role = 1 and  ev.series_id = " + seriesID + " " +
            "where ev.series_id = " + seriesID + " " +
            "group by pa.author_id) " + 
            "group by time order by time";
            
            ////System.out.println(query);
            ResultSet rs = this.executeQuery(query);
            
            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * @param seriesID
     * @return
     * @updated 13.05.2012 by Rizwan Uppal
     */
    public Color getColor(float x) {
            float r = 0.0f;
            float g = 0.0f;
            float b = 1.0f;
            if (x >= 0.0f && x < 0.2f) {
                    x = x / 0.2f;
                    r = 0.0f;
                    g = x;
                    b = 1.0f;
            } else if (x >= 0.2f && x < 0.4f) {
                    x = (x - 0.2f) / 0.2f;
                    r = 0.0f;
                    g = 1.0f;
                    b = 1.0f - x;
            } else if (x >= 0.4f && x < 0.6f) {
                    x = (x - 0.4f) / 0.2f;
                    r = x;
                    g = 1.0f;
                    b = 0.0f;
            } else if (x >= 0.6f && x < 0.8f) {
                    x = (x - 0.6f) / 0.2f;
                    r = 1.0f;
                    g = 1.0f - x;
                    b = 0.0f;
            } else if (x >= 0.8f && x <= 1.0f) {
                    x = (x - 0.8f) / 0.2f;
                    r = 1.0f;
                    g = 0.0f;
                    b = x;
            }
            return new Color(r, g, b);
    }
    
    /**
     * @param seriesID
     * @return
     * @updated 13.05.2012 by Rizwan Uppal
     */
    public double round3Decimals(double d) 
    {
        //DecimalFormat form = new DecimalFormat("#.###");
        //return Double.valueOf(form.format(d));
        return (double)Math.round(d * 1000) / 1000;
    }

}
