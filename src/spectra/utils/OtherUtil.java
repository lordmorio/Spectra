/*
 * Copyright 2016 jagrosh.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spectra.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.MiscUtil;
import spectra.Argument;
import spectra.Command;
import spectra.PermLevel;
import spectra.SpConst;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class OtherUtil { 
    
    public static ArrayList<String> readFileLines(String filename)
    {
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(filename));
        }catch(FileNotFoundException e){return null;}
        ArrayList<String> items = new ArrayList<>();
        try{
            while(true)
            {
                String next = reader.readLine();
                if(next==null)
                    break;
                items.add(next.trim());
            }
            reader.close();
            return items;
        }catch(IOException e){
            return null;
        }
    }
    
    public static ArrayList<String> readTrueFileLines(String filename)
    {
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(filename));
        }catch(FileNotFoundException e){return null;}
        ArrayList<String> items = new ArrayList<>();
        try{
            while(true)
            {
                String next = reader.readLine();
                if(next==null)
                    break;
                items.add(next);
            }
            reader.close();
            return items;
        }catch(IOException e){
            return null;
        }
    }
    
    public static File writeArchive(String text, String txtname)
    {
        File f = new File("WrittenFiles"+File.separatorChar+txtname+".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) 
        {
                String lines[] = text.split("\n");
                for(String line: lines)
                {
                    writer.write(line+"\r\n");
                }
                writer.flush();
        }catch(IOException e){System.err.println("ERROR saving file: "+e);}
        return f;
    }
    
    public static BufferedImage imageFromUrl(String url)
    {
        if(url==null)
            return null;
        try {
            URL u = new URL(url);
            URLConnection urlConnection = u.openConnection();
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36");
            //urlConnection.setRequestProperty("authorization", jda.getAuthToken());
            
            return ImageIO.read(urlConnection.getInputStream());
        } catch(IOException|IllegalArgumentException e) {
            System.err.println("[ERROR] Retrieving image: "+e);
        }
        return null;
    }
    
    public static BufferedImage makeWave(Color c)
    {
        BufferedImage bi = new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2d = bi.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, 128, 128);
        
        float radius = 28+(float)(Math.random()*4);
        float[] dist = {0.0f,.03f, .08f,.3f, 1.0f};
        Color[] colors = {new Color(255,255,255,255),new Color(255,255,255,255), 
            new Color(c.getRed(),c.getGreen(),c.getBlue(),15),new Color(c.getRed(),
                    c.getGreen(),c.getBlue(),5), new Color(c.getRed(),c.getGreen(),c.getBlue(),0)};
        int times = 2+(int)(Math.random()*3);
        for(int j=0;j<times;j++)
        {
            double accel=0;
            int height = 64;
            for(int i=-(int)radius;i<128+(int)radius;i++)
            {
                accel+=(Math.random()*2)-1;
                if(accel>2.1)
                    accel-=.3;
                if(accel<-2.1)
                    accel+=.3;
                if(height<48)
                    accel+=.7;
                if(height>80)
                    accel-=.7;
                height+=(int)accel;
                Point2D center = new Point2D.Float(i, height);


                RadialGradientPaint p =new RadialGradientPaint(center, radius, dist, colors,MultipleGradientPaint.CycleMethod.NO_CYCLE);
                g2d.setPaint(p);
                g2d.fillRect(0, 0, 128, 128);
            }
        }
        return bi;
    }
    
    public static File drawPlot(Guild guild, OffsetDateTime now)
    {
        long start = MiscUtil.getCreationTime(guild.getId()).toEpochSecond();
        long end = now.toEpochSecond();
        int width = 1000;
        int height = 600;
        List<User> joins = new ArrayList<>(guild.getUsers());
        Collections.sort(joins, (User a, User b) -> guild.getJoinDateForUser(a).compareTo(guild.getJoinDateForUser(b)));
        BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2d = bi.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, width, height);
        double lastX = 0;
        int lastY = height;

        for(int i=0; i<joins.size(); i++)
        {
            double x = (((guild.getJoinDateForUser(joins.get(i)).toEpochSecond() - start) * width) / (end-start));
            int y = height - ((i * height) / joins.size());
            double angle = (x==lastX) ? 1.0 : Math.tan((double)(lastY-y)/(x-lastX))/(Math.PI/2);
            g2d.setColor(Color.getHSBColor((float)angle/4, 1.0f, 1.0f));
            g2d.drawLine((int)x, y, (int)lastX, lastY);
            lastX=x;
            lastY=y;
        }
        g2d.setFont(g2d.getFont().deriveFont(24f));
        g2d.setColor(Color.WHITE);
        g2d.drawString("0 - "+joins.size()+" Users", 20, 26);
        g2d.drawString(MiscUtil.getCreationTime(guild.getId()).format(DateTimeFormatter.RFC_1123_DATE_TIME), 20, 60);
        g2d.drawString(now.format(DateTimeFormatter.RFC_1123_DATE_TIME), 20, 90);
        File f = new File("plot.png");
        try {
            ImageIO.write(bi, "png", f);
        } catch (IOException ex) {
            System.out.println("[ERROR] An error occured drawing the plot.");
        }
        return f;
    }
    
    public static String compileCommands(Command[] commands)
    {
        StringBuilder builder = new StringBuilder("Spectra Commands (v"+SpConst.VERSION+")\n");
        for(PermLevel level : PermLevel.values())
        {
            if(level==PermLevel.JAGROSH)
                continue;
            builder.append("\n# ").append(level==PermLevel.EVERYONE ? "User" : (level==PermLevel.MODERATOR ? "Moderator" : "Admin")).append(" Commands\n");
            for(Command cmd : commands)
            {
                if(level.isAtLeast(cmd.getLevel()))
                    builder.append(compileCommand(cmd,"","",level));
            }
        }
        return builder.toString();
    }
    
    private static String compileCommand(Command command, String lineStart, String parentchain, PermLevel level)
    {
        String fullCommand = parentchain.length()==0 ? command.getName() : parentchain+" "+command.getName();
        StringBuilder builder = new StringBuilder();
        if(lineStart.length()==0)
            builder.append("<a name=\"").append(command.getName()).append("\">**").append(fullCommand).append("**</a><br>\n");
        else
            builder.append(lineStart).append("**").append(fullCommand).append("**<br>\n");
        
        if(level == command.getLevel())
        {
            builder.append(lineStart).append("Usage: `").append(SpConst.PREFIX).append(fullCommand).append(Argument.arrayToString(command.getArguments())).append("`<br>\n");
            if(command.getAliases().length>0)
            {
                builder.append("Aliases:");
                for(String alias : command.getAliases())
                    builder.append(" ").append(alias);
                builder.append("<br>\n");
            }
            builder.append(lineStart).append("*").append(command.getLongHelp()).append("*\n\n");
        }
        else if(command.getChildren().length == 0)
            return "";
        
        StringBuilder childBuilder = new StringBuilder();
        for(Command cmd: command.getChildren())
            if(level.isAtLeast(cmd.getLevel()))
                childBuilder.append(compileCommand(cmd,lineStart+"> ",fullCommand,level));
        if(level != command.getLevel() && childBuilder.length() == 0)
            return "";
        return builder.toString()+childBuilder.toString();
    }
}
