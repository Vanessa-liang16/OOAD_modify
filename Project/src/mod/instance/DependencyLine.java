package mod.instance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.BasicStroke;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import Define.AreaDefine;
import Pack.DragPack;
import bgWork.handler.CanvasPanelHandler;
import mod.IFuncComponent;
import mod.ILinePainter;
import java.lang.Math;

public class DependencyLine extends JPanel
        implements IFuncComponent, ILinePainter
{
    JPanel              from;
    int                 fromSide;
    Point               fp              = new Point(0, 0);
    JPanel              to;
    int                 toSide;
    Point               tp              = new Point(0, 0);
    int                 arrowSize       = 12;
    int                 panelExtendSize = 10;
    boolean             isSelect        = false;
    boolean             isHighlight     = false;  // 加入 highlight 狀態
    int                 selectBoxSize   = 5;
    CanvasPanelHandler  cph;
    float[]             dashPattern     = {5.0f, 5.0f}; // Define dash pattern for dashed line

    public DependencyLine(CanvasPanelHandler cph)
    {
        this.setOpaque(false);
        this.setVisible(true);
        this.setMinimumSize(new Dimension(1, 1));
        this.cph = cph;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        Point fpPrime;
        Point tpPrime;
        renewConnect();
        fpPrime = new Point(fp.x - this.getLocation().x,
                fp.y - this.getLocation().y);
        tpPrime = new Point(tp.x - this.getLocation().x,
                tp.y - this.getLocation().y);
        
        // Cast to Graphics2D
        Graphics2D g2d = (Graphics2D) g;
        
        // 根據 highlight 狀態設定顏色
        if (isHighlight) {
            g2d.setColor(Color.RED); // Highlight 時使用紅色
        } else {
            g2d.setColor(Color.BLACK); // 正常狀態使用黑色
        }
        
        // Save original stroke
        java.awt.Stroke originalStroke = g2d.getStroke();
        
        // Set dashed stroke
        float[] dash = {5.0f, 5.0f};
        BasicStroke dashedStroke = new BasicStroke(
            isHighlight ? 2.0f : 1.0f,  // Highlight 時加粗
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f,
            dash,
            0.0f
        );
        
        g2d.setStroke(dashedStroke);
        
        // Draw the dashed line
        g2d.drawLine(fpPrime.x, fpPrime.y, tpPrime.x, tpPrime.y);
        
        // Restore original stroke for the arrow
        g2d.setStroke(originalStroke);
        
        // Draw the arrow with correct calculation
        paintArrow(g2d, fpPrime, tpPrime);
        
        // Draw selection boxes if selected
        if (isSelect == true)
        {
            paintSelect(g);
        }
    }

    @Override
    public void reSize()
    {
        Dimension size = new Dimension(
                Math.abs(fp.x - tp.x) + panelExtendSize * 2,
                Math.abs(fp.y - tp.y) + panelExtendSize * 2);
        this.setSize(size);
        this.setLocation(Math.min(fp.x, tp.x) - panelExtendSize,
                Math.min(fp.y, tp.y) - panelExtendSize);
    }

    // 新的箭頭繪製方法，使用正確的起點和終點
    public void paintArrow(Graphics2D g2d, Point fpPrime, Point tpPrime)
    {
        // 設定箭頭線條粗細
        g2d.setStroke(new BasicStroke(2.0f));
        
        // 計算線條的角度（從 fpPrime 到 tpPrime）
        double dx = tpPrime.x - fpPrime.x;
        double dy = tpPrime.y - fpPrime.y;
        double angle = Math.atan2(dy, dx);
        
        // 計算箭頭的兩個端點（從終點往回算）
        int x1 = tpPrime.x - (int)(arrowSize * Math.cos(angle - Math.PI/5));
        int y1 = tpPrime.y - (int)(arrowSize * Math.sin(angle - Math.PI/5));
        int x2 = tpPrime.x - (int)(arrowSize * Math.cos(angle + Math.PI/5));
        int y2 = tpPrime.y - (int)(arrowSize * Math.sin(angle + Math.PI/5));
        
        // 繪製箭頭，使用相同的顏色
        g2d.drawLine(tpPrime.x, tpPrime.y, x1, y1);
        g2d.drawLine(tpPrime.x, tpPrime.y, x2, y2);
    }

    @Override
    public void paintArrow(Graphics g, Point point)
    {
        // 為了符合介面要求，但實際上不使用這個方法
        // 真正的繪製在 paintComponent 中呼叫 paintArrow(Graphics2D, Point, Point)
    }

    @Override
    public void setConnect(DragPack dPack)
    {
        
        Point mfp = dPack.getFrom();
        Point mtp = dPack.getTo();
        from = (JPanel) dPack.getFromObj();
        to = (JPanel) dPack.getToObj();
        
        // 使用拖曳的實際點來計算 side
        // 將滑鼠點擊位置轉換為相對於物件的位置
        Point relativeFrom = new Point(
            mfp.x - from.getLocation().x,
            mfp.y - from.getLocation().y
        );
        Point relativeTo = new Point(
            mtp.x - to.getLocation().x,
            mtp.y - to.getLocation().y
        );
        
        fromSide = new AreaDefine().getArea(new Point(0, 0), from.getSize(), relativeFrom);
        toSide = new AreaDefine().getArea(new Point(0, 0), to.getSize(), relativeTo);
        
        
        renewConnect();
    }

    void renewConnect()
    {
        try
        {
            fp = getConnectPoint(from, fromSide);
            tp = getConnectPoint(to, toSide);
            this.reSize();
        }
        catch (NullPointerException e)
        {
            this.setVisible(false);
            cph.removeComponent(this);
        }
    }

    Point getConnectPoint(JPanel jp, int side)
    {
        Point temp = new Point(0, 0);
        Point jpLocation = cph.getAbsLocation(jp);
        if (side == new AreaDefine().TOP)
        {
            temp.x = (int) (jpLocation.x + jp.getSize().getWidth() / 2);
            temp.y = jpLocation.y;
        }
        else if (side == new AreaDefine().RIGHT)
        {
            temp.x = (int) (jpLocation.x + jp.getSize().getWidth());
            temp.y = (int) (jpLocation.y + jp.getSize().getHeight() / 2);
        }
        else if (side == new AreaDefine().LEFT)
        {
            temp.x = jpLocation.x;
            temp.y = (int) (jpLocation.y + jp.getSize().getHeight() / 2);
        }
        else if (side == new AreaDefine().BOTTOM)
        {
            temp.x = (int) (jpLocation.x + jp.getSize().getWidth() / 2);
            temp.y = (int) (jpLocation.y + jp.getSize().getHeight());
        }
        else
        {
            temp = null;
            System.err.println("getConnectPoint fail:" + side);
        }
        return temp;
    }

    @Override
    public void paintSelect(Graphics gra)
    {
        gra.setColor(Color.BLACK);
        gra.fillRect(fp.x - selectBoxSize/2 - this.getLocation().x, 
                    fp.y - selectBoxSize/2 - this.getLocation().y, 
                    selectBoxSize, selectBoxSize);
        gra.fillRect(tp.x - selectBoxSize/2 - this.getLocation().x, 
                    tp.y - selectBoxSize/2 - this.getLocation().y, 
                    selectBoxSize, selectBoxSize);
    }

    public boolean isSelect()
    {
        return isSelect;
    }

    public void setSelect(boolean isSelect)
    {
        this.isSelect = isSelect;
    }
    
    // 加入 highlight 相關的 getter 和 setter
    public boolean isHighlight()
    {
        return isHighlight;
    }
    
    public void setHighlight(boolean highlight)
    {
        this.isHighlight = highlight;
    }
    
    // 加入存取 from, to, fromSide, toSide 的 getter 方法
    public JPanel getFrom()
    {
        return from;
    }
    
    public JPanel getTo()
    {
        return to;
    }
    
    public int getFromSide()
    {
        return fromSide;
    }
    
    public int getToSide()
    {
        return toSide;
    }
}
