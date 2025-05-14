package bgWork.handler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import Define.AreaDefine;
import Listener.CPHActionListener;   
import Pack.DragPack;
import Pack.SendText;
import bgWork.InitProcess;
import mod.instance.AssociationLine;
import mod.instance.BasicClass;
import mod.instance.CompositionLine;
import mod.instance.GeneralizationLine;
import mod.instance.DependencyLine;   //ADD
import mod.instance.GroupContainer;
import mod.instance.UseCase;

public class CanvasPanelHandler extends PanelHandler
{
	Vector <JPanel>	members		= new Vector <>();
	Vector <JPanel>	selectComp	= new Vector <>();
	int				boundShift	= 10;

	public CanvasPanelHandler(JPanel Container, InitProcess process)
	{
		super(Container, process);
		boundDistance = 10;
		initContextPanel();
		Container.add(this.contextPanel);
	}

	@Override
	void initContextPanel()
	{
		JPanel fphContextPanel = core.getFuncPanelHandler().getContectPanel();
		contextPanel = new JPanel();
		contextPanel.setBounds(
				fphContextPanel.getLocation().x
						+ fphContextPanel.getSize().width + boundShift,
				fphContextPanel.getLocation().y, 800, 600);
		contextPanel.setLayout(null);
		contextPanel.setVisible(true);
		contextPanel.setBackground(Color.WHITE);
		contextPanel.setBorder(new LineBorder(Color.BLACK));
		contextPanel.addMouseListener(new CPHActionListener(this));
	}

	@Override
	public void ActionPerformed(MouseEvent e)
	{
		switch (core.getCurrentFuncIndex())
		{
			case 0:
				selectByClick(e);
				break;
			case 1:
			case 2:
			case 3:
			case 6:   //ADD
				break;
			case 4:
			case 5:
				addObject(core.getCurrentFunc(), e.getPoint());
				break;
			default:
				break;
		}
		repaintComp();
	}

	public void ActionPerformed(DragPack dp)
	{
		switch (core.getCurrentFuncIndex())
		{
			case 0:
				selectByDrag(dp);
				break;
			case 1:
			case 2:
			case 3:
			case 6:   //ADD
				addLine(core.getCurrentFunc(), dp);
				break;
			case 4:
			case 5:
				break;
			default:
				break;
		}
		repaintComp();
	}

	public void repaintComp()
	{
		for (int i = 0; i < members.size(); i ++)
		{
			members.elementAt(i).repaint();
		}
		contextPanel.updateUI();
	}

	// void selectByClick(MouseEvent e)
	// {
	// 	boolean isSelect = false;
	// 	selectComp = new Vector <>();
	// 	for (int i = 0; i < members.size(); i ++)
	// 	{
	// 		if (isInside(members.elementAt(i), e.getPoint()) == true
	// 				&& isSelect == false)
	// 		{
	// 			switch (core.isFuncComponent(members.elementAt(i)))
	// 			{
	// 				case 0:
	// 					((BasicClass) members.elementAt(i)).setSelect(true);
	// 					selectComp.add(members.elementAt(i));
	// 					isSelect = true;
	// 					break;
	// 				case 1:
	// 					((UseCase) members.elementAt(i)).setSelect(true);
	// 					selectComp.add(members.elementAt(i));
	// 					isSelect = true;
	// 					break;
	// 				case 5:
	// 					Point p = e.getPoint();
	// 					p.x -= members.elementAt(i).getLocation().x;
	// 					p.y -= members.elementAt(i).getLocation().y;
	// 					if (groupIsSelect((GroupContainer) members.elementAt(i),
	// 							p))
	// 					{
	// 						((GroupContainer) members.elementAt(i))
	// 								.setSelect(true);
	// 						selectComp.add(members.elementAt(i));
	// 						isSelect = true;
	// 					}
	// 					else
	// 					{
	// 						((GroupContainer) members.elementAt(i))
	// 								.setSelect(false);
	// 					}
	// 					break;
	// 				default:
	// 					break;
	// 			}
	// 		}
	// 		else
	// 		{
	// 			setSelectAllType(members.elementAt(i), false);
	// 		}
	// 	}
	// 	repaintComp();
	// }
	void selectByClick(MouseEvent e)
	{
		System.out.println("selectByClick 被呼叫，點擊位置: " + e.getPoint());
		
		boolean isSelect = false;
		selectComp = new Vector<>();
		
		// 先檢查是否點擊在 port 上
		for (int i = 0; i < members.size(); i++)
		{
			JPanel member = members.elementAt(i);
			if (isInside(member, e.getPoint()))
			{
				System.out.println("點擊在物件 " + member.getClass().getSimpleName() + " 內");
				int port = isClickOnPort(member, e.getPoint());
				if (port != -1)
				{
					System.out.println("點擊在 port " + port + " 上");
					// 點擊在 port 上，highlight 相連的線條
					highlightConnectedLines(member, port);
					return; // 不進行選擇操作
				}
			}
		}
		
		// 如果不是點擊在 port 上，清除所有 highlight
		System.out.println("不是點擊在 port 上，清除 highlight");
		clearAllHighlights();
		
		// 原有的選擇邏輯
		for (int i = 0; i < members.size(); i++)
		{
			if (isInside(members.elementAt(i), e.getPoint()) == true
					&& isSelect == false)
			{
				switch (core.isFuncComponent(members.elementAt(i)))
				{
					case 0:
						((BasicClass) members.elementAt(i)).setSelect(true);
						selectComp.add(members.elementAt(i));
						isSelect = true;
						break;
					case 1:
						((UseCase) members.elementAt(i)).setSelect(true);
						selectComp.add(members.elementAt(i));
						isSelect = true;
						break;
					case 5:
						Point p = e.getPoint();
						p.x -= members.elementAt(i).getLocation().x;
						p.y -= members.elementAt(i).getLocation().y;
						if (groupIsSelect((GroupContainer) members.elementAt(i), p))
						{
							((GroupContainer) members.elementAt(i)).setSelect(true);
							selectComp.add(members.elementAt(i));
							isSelect = true;
						}
						else
						{
							((GroupContainer) members.elementAt(i)).setSelect(false);
						}
						break;
					default:
						break;
				}
			}
			else
			{
				setSelectAllType(members.elementAt(i), false);
			}
		}
		repaintComp();
	}

	boolean groupIsSelect(GroupContainer container, Point point)
	{
		for (int i = 0; i < container.getComponentCount(); i ++)
		{
			if (core.isGroupContainer(container.getComponent(i)))
			{
				point.x -= container.getComponent(i).getLocation().x;
				point.y -= container.getComponent(i).getLocation().y;
				if (groupIsSelect((GroupContainer) container.getComponent(i),
						point) == true)
				{
					return true;
				}
				else
				{
					point.x += container.getComponent(i).getLocation().x;
					point.y += container.getComponent(i).getLocation().y;
				}
			}
			else if (core.isJPanel(container.getComponent(i)))
			{
				if (isInside((JPanel) container.getComponent(i), point))
				{
					return true;
				}
			}
		}
		return false;
	}

	boolean selectByDrag(DragPack dp)
	{
		if (isInSelect(dp.getFrom()) == true)
		{
			// dragging components
			Dimension shift = new Dimension(dp.getTo().x - dp.getFrom().x,
					dp.getTo().y - dp.getFrom().y);
			for (int i = 0; i < selectComp.size(); i ++)
			{
				JPanel jp = selectComp.elementAt(i);
				jp.setLocation(jp.getLocation().x + shift.width,
						jp.getLocation().y + shift.height);
				if (jp.getLocation().x < 0)
				{
					jp.setLocation(0, jp.getLocation().y);
				}
				if (jp.getLocation().y < 0)
				{
					jp.setLocation(jp.getLocation().x, 0);
				}
			}
			return true;
		}
		if (dp.getFrom().x > dp.getTo().x && dp.getFrom().y > dp.getTo().y)
		{
			// drag right down from to left up
			groupInversSelect(dp);
			return true;
		}
		else if (dp.getFrom().x < dp.getTo().x && dp.getFrom().y < dp.getTo().y)
		{
			// drag from left up to right down
			groupSelect(dp);
			return true;
		}
		return false;
	}

	public void setGroup()
	{
		if (selectComp.size() > 1)
		{
			GroupContainer gContainer = new GroupContainer(core);
			gContainer.setVisible(true);
			Point p1 = new Point(selectComp.elementAt(0).getLocation().x,
					selectComp.elementAt(0).getLocation().y);
			Point p2 = new Point(selectComp.elementAt(0).getLocation().x,
					selectComp.elementAt(0).getLocation().y);
			Point testP;
			for (int i = 0; i < selectComp.size(); i ++)
			{
				testP = selectComp.elementAt(i).getLocation();
				if (p1.x > testP.x)
				{
					p1.x = testP.x;
				}
				if (p1.y > testP.y)
				{
					p1.y = testP.y;
				}
				if (p2.x < testP.x + selectComp.elementAt(i).getSize().width)
				{
					p2.x = testP.x + selectComp.elementAt(i).getSize().width;
				}
				if (p2.y < testP.y + selectComp.elementAt(i).getSize().height)
				{
					p2.y = testP.y + selectComp.elementAt(i).getSize().height;
				}
			}
			p1.x --;
			p1.y --;
			gContainer.setLocation(p1);
			gContainer.setSize(p2.x - p1.x + 2, p2.y - p1.y + 2);
			for (int i = 0; i < selectComp.size(); i ++)
			{
				JPanel temp = selectComp.elementAt(i);
				removeComponent(temp);
				gContainer.add(temp, i);
				temp.setLocation(temp.getLocation().x - p1.x,
						temp.getLocation().y - p1.y);
			}
			addComponent(gContainer);
			selectComp = new Vector <>();
			selectComp.add(gContainer);
			repaintComp();
		}
	}

	public void setUngroup()
	{
		int size = selectComp.size();
		for (int i = 0; i < size; i ++)
		{
			if (core.isGroupContainer(selectComp.elementAt(i)))
			{
				GroupContainer gContainer = (GroupContainer) selectComp
						.elementAt(i);
				Component temp;
				int j = 0;
				while (gContainer.getComponentCount() > 0)
				{
					temp = gContainer.getComponent(0);
					temp.setLocation(
							temp.getLocation().x + gContainer.getLocation().x,
							temp.getLocation().y + gContainer.getLocation().y);
					addComponent((JPanel) temp, j);
					selectComp.add((JPanel) temp);
					gContainer.remove(temp);
					j ++;
				}
				removeComponent(gContainer);
				selectComp.remove(gContainer);
			}
			repaintComp();
		}
	}

	void groupSelect(DragPack dp)
	{
		JPanel jp = new JPanel();
		jp.setLocation(dp.getFrom());
		jp.setSize(Math.abs(dp.getTo().x - dp.getFrom().x),
				Math.abs(dp.getTo().y - dp.getFrom().x));
		selectComp = new Vector <>();
		for (int i = 0; i < members.size(); i ++)
		{
			if (isInside(jp, members.elementAt(i)) == true)
			{
				selectComp.add(members.elementAt(i));
				setSelectAllType(members.elementAt(i), true);
			}
			else
			{
				setSelectAllType(members.elementAt(i), false);
			}
		}
	}

	void groupInversSelect(DragPack dp)
	{
		JPanel jp = new JPanel();
		jp.setLocation(dp.getTo());
		jp.setSize(Math.abs(dp.getTo().x - dp.getFrom().x),
				Math.abs(dp.getTo().y - dp.getFrom().x));
		selectComp = new Vector <>();
		for (int i = 0; i < members.size(); i ++)
		{
			if (isInside(jp, members.elementAt(i)) == false)
			{
				selectComp.add(members.elementAt(i));
				setSelectAllType(members.elementAt(i), true);
			}
			else
			{
				setSelectAllType(members.elementAt(i), false);
			}
		}
	}

	boolean isInSelect(Point point)
	{
		for (int i = 0; i < selectComp.size(); i ++)
		{
			if (isInside(selectComp.elementAt(i), point) == true)
			{
				return true;
			}
		}
		return false;
	}

	void addLine(JPanel funcObj, DragPack dPack)
	{
		
		for (int i = 0; i < members.size(); i++)
		{
			if (isInside(members.elementAt(i), dPack.getFrom()) == true)
			{
				dPack.setFromObj(members.elementAt(i));
			}
			if (isInside(members.elementAt(i), dPack.getTo()) == true)
			{
				dPack.setToObj(members.elementAt(i));
			}
		}
		
		if (dPack.getFromObj() == dPack.getToObj()
				|| dPack.getFromObj() == contextPanel
				|| dPack.getToObj() == contextPanel)
		{
			return;
		}
		
		switch (members.size())
		{
			case 0:
			case 1:
				System.out.println("成員數量不足");
				break;
			default:
				int lineType = core.isLine(funcObj);
				System.out.println("線條類型: " + lineType);
				
				switch (lineType)
				{
					case 0:
						((AssociationLine) funcObj).setConnect(dPack);
						break;
					case 1:
						((CompositionLine) funcObj).setConnect(dPack);
						break;
					case 2:
						((GeneralizationLine) funcObj).setConnect(dPack);
						break;
					case 3:
						((DependencyLine) funcObj).setConnect(dPack);
						break;
					default:
						System.out.println("未知的線條類型");
						break;
				}
				
				// 加入線條到 members
				if (members.contains(funcObj))
				{
					System.out.println("線條已經在 members 中");
				}
				else
				{
					members.add(funcObj);
					System.out.println("線條加入到 members");
				}
				
				contextPanel.add(funcObj, 0);
				System.out.println("線條加入到面板");
				break;
		}
	
	}

	void addObject(JPanel funcObj, Point point)
	{
		if (members.size() > 0)
		{
			members.insertElementAt(funcObj, 0);
		}
		else
		{
			members.add(funcObj);
		}
		members.elementAt(0).setLocation(point);
		members.elementAt(0).setVisible(true);
		contextPanel.add(members.elementAt(0), 0);
	}

	public boolean isInside(JPanel container, Point point)
	{
		Point cLocat = container.getLocation();
		Dimension cSize = container.getSize();
		if (point.x >= cLocat.x && point.y >= cLocat.y)
		{
			if (point.x <= cLocat.x + cSize.width
					&& point.y <= cLocat.y + cSize.height)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isInside(JPanel container, JPanel test)
	{
		Point cLocat = container.getLocation();
		Dimension cSize = container.getSize();
		Point tLocat = test.getLocation();
		Dimension tSize = test.getSize();
		if (cLocat.x <= tLocat.x && cLocat.y <= tLocat.y)
		{
			if (cLocat.x + cSize.width >= tLocat.x + tSize.width
					&& cLocat.y + cSize.height >= tLocat.y + tSize.height)
			{
				return true;
			}
		}
		return false;
	}

	public JPanel getSingleSelectJP()
	{
		if (selectComp.size() == 1)
		{
			return selectComp.elementAt(0);
		}
		return null;
	}

	public void setContext(SendText tr)
	{
		System.out.println(tr.getText());
		try
		{
			switch (core.isClass(tr.getDest()))
			{
				case 0:
					((BasicClass) tr.getDest()).setText(tr.getText());
					break;
				case 1:
					((UseCase) tr.getDest()).setText(tr.getText());
					break;
				default:
					break;
			}
		}
		catch (Exception e)
		{
			System.err.println("CPH error");
		}
	}

	void addComponent(JPanel comp)
	{
		contextPanel.add(comp, 0);
		members.insertElementAt(comp, 0);
	}

	void addComponent(JPanel comp, int index)
	{
		contextPanel.add(comp, index);
		members.insertElementAt(comp, index);
	}

	public void removeComponent(JPanel comp)
	{
		contextPanel.remove(comp);
		members.remove(comp);
	}

	void setSelectAllType(Object obj, boolean isSelect)
	{
		switch (core.isFuncComponent(obj))
		{
			case 0:
				((BasicClass) obj).setSelect(isSelect);
				break;
			case 1:
				((UseCase) obj).setSelect(isSelect);
				break;
			case 2:
				((AssociationLine) obj).setSelect(isSelect);
				break;
			case 3:
				((CompositionLine) obj).setSelect(isSelect);
				break;
			case 4:
				((GeneralizationLine) obj).setSelect(isSelect);
				break;
			case 5:
				((GroupContainer) obj).setSelect(isSelect);
				break;
			case 6: // Add this case for DependencyLine
				((DependencyLine) obj).setSelect(isSelect);
				break;
			default:
				break;
		}
	}

	public Point getAbsLocation(Container panel)
	{
		Point location = panel.getLocation();
		while (panel.getParent() != contextPanel)
		{
			panel = panel.getParent();
			location.x += panel.getLocation().x;
			location.y += panel.getLocation().y;
		}
		return location;
	}

	//ADD
	// 檢查是否點擊在 port 上
	public int isClickOnPort(JPanel component, Point clickPoint)
	{
		if (component instanceof BasicClass || component instanceof UseCase)
		{
			Point componentLocation = component.getLocation();
			Point relativeClick = new Point(
				clickPoint.x - componentLocation.x,
				clickPoint.y - componentLocation.y
			);
			
			Dimension componentSize = component.getSize();
			int portSize = 5; // 與 selectBoxSize 相同
			int tolerance = 3; // 增加點擊容錯範圍
			
			// 檢查上方 port
			if (relativeClick.x >= componentSize.width/2 - portSize - tolerance &&
				relativeClick.x <= componentSize.width/2 + portSize + tolerance &&
				relativeClick.y >= -tolerance && 
				relativeClick.y <= portSize + tolerance)
			{
				return new AreaDefine().TOP;
			}
			
			// 檢查右方 port
			if (relativeClick.x >= componentSize.width - portSize - tolerance &&
				relativeClick.x <= componentSize.width + tolerance &&
				relativeClick.y >= componentSize.height/2 - portSize - tolerance &&
				relativeClick.y <= componentSize.height/2 + portSize + tolerance)
			{
				return new AreaDefine().RIGHT;
			}
			
			// 檢查左方 port
			if (relativeClick.x >= -tolerance && 
				relativeClick.x <= portSize + tolerance &&
				relativeClick.y >= componentSize.height/2 - portSize - tolerance &&
				relativeClick.y <= componentSize.height/2 + portSize + tolerance)
			{
				return new AreaDefine().LEFT;
			}
			
			// 檢查下方 port
			if (relativeClick.x >= componentSize.width/2 - portSize - tolerance &&
				relativeClick.x <= componentSize.width/2 + portSize + tolerance &&
				relativeClick.y >= componentSize.height - portSize - tolerance &&
				relativeClick.y <= componentSize.height + tolerance)
			{
				return new AreaDefine().BOTTOM;
			}
		}
		return -1; // 不在任何 port 上
	}

	
	// Highlight 與特定物件和 port 相連的線條
	public void highlightConnectedLines(JPanel component, int port)
	{
		
		// 先清除所有線條的 highlight
		clearAllHighlights();
		
		int highlightCount = 0;
		int lineCount = 0;
		
		// 遍歷所有成員，找出線條
		for (int i = 0; i < members.size(); i++)
		{
			JPanel member = members.get(i);
			System.out.println("檢查成員[" + i + "]: " + member.getClass().getSimpleName());
			
			int lineType = core.isLine(member);
			System.out.println("  isLine 結果: " + lineType);
			
			if (lineType >= 0) // 是線條
			{
				lineCount++;
				System.out.println("  這是一條線，類型: " + lineType);
				boolean shouldHighlight = false;
				
				// 檢查各種類型的線條
				switch (lineType)
				{
					case 0: // AssociationLine
						AssociationLine aLine = (AssociationLine) member;
						if ((aLine.getFrom() == component && aLine.getFromSide() == port) ||
							(aLine.getTo() == component && aLine.getToSide() == port))
						{
							aLine.setHighlight(true);
							shouldHighlight = true;
						}
						break;
						
					case 1: // CompositionLine
						CompositionLine cLine = (CompositionLine) member;
						if ((cLine.getFrom() == component && cLine.getFromSide() == port) ||
							(cLine.getTo() == component && cLine.getToSide() == port))
						{
							cLine.setHighlight(true);
							shouldHighlight = true;
						}
						break;
						
					case 2: // GeneralizationLine
						GeneralizationLine gLine = (GeneralizationLine) member;
						if ((gLine.getFrom() == component && gLine.getFromSide() == port) ||
							(gLine.getTo() == component && gLine.getToSide() == port))
						{
							gLine.setHighlight(true);
							shouldHighlight = true;
						}
						break;
						
					case 3: // DependencyLine
						DependencyLine dLine = (DependencyLine) member;
						if ((dLine.getFrom() == component && dLine.getFromSide() == port) ||
							(dLine.getTo() == component && dLine.getToSide() == port))
						{
							dLine.setHighlight(true);
							shouldHighlight = true;
						}
						break;
				}
				
				if (shouldHighlight)
				{
					highlightCount++;
					member.repaint();
				}
			}
		}
		
	}


	// 清除所有線條的 highlight
	public void clearAllHighlights()
	{
		for (JPanel member : members)
		{
			if (core.isLine(member) >= 0)
			{
				switch (core.isLine(member))
				{
					case 0:
						((AssociationLine) member).setHighlight(false);
						break;
					case 1:
						((CompositionLine) member).setHighlight(false);
						break;
					case 2:
						((GeneralizationLine) member).setHighlight(false);
						break;
					case 3:
						((DependencyLine) member).setHighlight(false);
						break;
				}
				member.repaint();
			}
		}
	}

}
