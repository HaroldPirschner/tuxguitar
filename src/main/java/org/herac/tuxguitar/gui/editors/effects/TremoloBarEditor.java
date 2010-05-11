/*
 * Created on 28-dic-2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.herac.tuxguitar.gui.editors.effects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.editors.TGPainter;
import org.herac.tuxguitar.gui.util.DialogUtils;
import org.herac.tuxguitar.song.models.TGNote;
import org.herac.tuxguitar.song.models.effects.BendingEffect;
import org.herac.tuxguitar.song.models.effects.EffectPoint;

/**
 * @author julian
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class TremoloBarEditor {
  private class DefaultTremoloBar {
    private String name;
    private BendingEffect tremoloBar;

    public DefaultTremoloBar(String name, BendingEffect tremoloBar) {
      this.name = name;
      this.tremoloBar = tremoloBar;
    }

    public String getName() {
      return this.name;
    }

    public BendingEffect getTremoloBar() {
      return this.tremoloBar;
    }
  }

  private static final int X_LENGTH = EffectPoint.MAX_POSITION_LENGTH + 1;
  public static final int X_SPACING = 30;
  private static final int Y_LENGTH = (EffectPoint.MAX_VALUE_LENGTH * 2) + 1;

  public static final int Y_SPACING = 10;
  protected DefaultTremoloBar[] defaultTremoloBars;
  protected Composite editor;
  private int height;
  private List<Point> points;
  protected BendingEffect result;
  private int width;
  private int[] x;

  private int[] y;

  public TremoloBarEditor() {
    this.init();
  }

  private void addPoint(Point point) {
    this.points.add(point);
  }

  private void addTremoloBarPoint(BendingEffect effect, Point point) {
    int position = 0;
    int value = 0;
    for (int i = 0; i < this.x.length; i++) {
      if (point.x == this.x[i]) {
        position = i;
      }
    }
    for (int i = 0; i < this.y.length; i++) {
      if (point.y == this.y[i]) {
        value = (EffectPoint.MAX_VALUE_LENGTH - i);
      }
    }
    effect.addPoint(position, value);
  }

  protected void checkPoint(int x, int y) {
    Point point = new Point(this.getX(x), this.getY(y));
    if (!this.removePoint(point)) {
      this.removePointsAtXLine(point.x);
      this.addPoint(point);
      this.orderPoints();
    }
  }

  public int getHeight() {
    return this.height;
  }

  public BendingEffect getTremoloBar() {
    if (this.points != null && !this.points.isEmpty()) {
      BendingEffect tremoloBar = new BendingEffect();
      for (final Point point : this.points) {
        addTremoloBarPoint(tremoloBar, point);
      }
      return tremoloBar;
    }
    return null;
  }

  public int getWidth() {
    return this.width;
  }

  private int getX(int pointX) {
    int currPointX = -1;
    for (int i = 0; i < this.x.length; i++) {
      if (currPointX < 0) {
        currPointX = this.x[i];
      } else {
        int distanceX = Math.abs(pointX - currPointX);
        int currDistanceX = Math.abs(pointX - this.x[i]);
        if (currDistanceX < distanceX) {
          currPointX = this.x[i];
        }
      }
    }
    return currPointX;
  }

  private int getY(int pointY) {
    int currPointY = -1;
    for (int i = 0; i < this.y.length; i++) {
      if (currPointY < 0) {
        currPointY = this.y[i];
      } else {
        int distanceX = Math.abs(pointY - currPointY);
        int currDistanceX = Math.abs(pointY - this.y[i]);
        if (currDistanceX < distanceX) {
          currPointY = this.y[i];
        }
      }
    }
    return currPointY;
  }

  private void init() {
    this.x = new int[X_LENGTH];
    this.y = new int[Y_LENGTH];
    this.width = ((X_SPACING * X_LENGTH) - X_SPACING);
    this.height = ((Y_SPACING * Y_LENGTH) - Y_SPACING);
    this.points = new ArrayList<Point>();

    for (int i = 0; i < this.x.length; i++) {
      this.x[i] = ((i + 1) * X_SPACING);
    }
    for (int i = 0; i < this.y.length; i++) {
      this.y[i] = ((i + 1) * Y_SPACING);
    }
  }

  public boolean isEmpty() {
    return this.points.isEmpty();
  }

  private void makePoint(EffectPoint tremoloBarPoint) {
    int indexX = tremoloBarPoint.getPosition();
    int indexY = ((this.y.length - EffectPoint.MAX_VALUE_LENGTH) - tremoloBarPoint
        .getValue()) - 1;
    if (indexX >= 0 && indexX < this.x.length && indexY >= 0
        && indexY < this.y.length) {
      Point point = new Point(0, 0);
      point.x = this.x[indexX];
      point.y = this.y[indexY];
      this.points.add(point);
    }
  }

  private void orderPoints() {
    for (int i = 0; i < this.points.size(); i++) {
      Point minPoint = null;
      for (int noteIdx = i; noteIdx < this.points.size(); noteIdx++) {
        Point point = (Point) this.points.get(noteIdx);
        if (minPoint == null || point.x < minPoint.x) {
          minPoint = point;
        }
      }
      this.points.remove(minPoint);
      this.points.add(i, minPoint);
    }
  }

  protected void paintEditor(TGPainter painter) {
    for (int i = 0; i < this.x.length; i++) {
      this.setStyleX(painter, i);
      painter.initPath();
      painter.setAntialias(false);
      painter.moveTo(this.x[i], Y_SPACING);
      painter.lineTo(this.x[i], Y_SPACING + this.height);
      painter.closePath();
    }
    for (int i = 0; i < this.y.length; i++) {
      this.setStyleY(painter, i);
      painter.initPath();
      painter.setAntialias(false);
      painter.moveTo(X_SPACING, this.y[i]);
      painter.lineTo(X_SPACING + this.width, this.y[i]);
      painter.closePath();
    }

    Point prevPoint = null;
    painter.setLineStyle(SWT.LINE_SOLID);
    painter.setLineWidth(2);
    painter.setForeground(this.editor.getDisplay().getSystemColor(
        SWT.COLOR_GRAY));
    for (final Point point : this.points) {
      if (prevPoint != null) {
        painter.initPath();
        painter.moveTo(prevPoint.x, prevPoint.y);
        painter.lineTo(point.x, point.y);
        painter.closePath();
      }
      prevPoint = point;
    }

    painter.setLineWidth(5);
    painter.setForeground(this.editor.getDisplay().getSystemColor(
        SWT.COLOR_BLACK));
    for (final Point point : this.points) {
      painter.initPath();
      painter.setAntialias(false);
      painter.addRectangle(point.x - 2, point.y - 2, 5, 5);
      painter.closePath();
    }
    painter.setLineWidth(1);
  }

  private boolean removePoint(Point point) {
    Iterator<Point> it = this.points.iterator();
    while (it.hasNext()) {
      Point currPoint = it.next();
      if (currPoint.x == point.x && currPoint.y == point.y) {
        this.points.remove(point);
        return true;
      }
    }
    return false;
  }

  private void removePointsAtXLine(int x) {
    Iterator<Point> it = this.points.iterator();
    while (it.hasNext()) {
      Point point = it.next();
      if (point.x == x) {
        this.points.remove(point);
        break;
      }
    }
  }

  private void resetDefaultTremoloBars() {
    this.defaultTremoloBars = new DefaultTremoloBar[6];

    this.defaultTremoloBars[0] = new DefaultTremoloBar(TuxGuitar
        .getProperty("effects.tremolo-bar.dip"), new BendingEffect());
    this.defaultTremoloBars[0].getTremoloBar().addPoint(0, 0);
    this.defaultTremoloBars[0].getTremoloBar().addPoint(6, -2);
    this.defaultTremoloBars[0].getTremoloBar().addPoint(12, 0);

    this.defaultTremoloBars[1] = new DefaultTremoloBar(TuxGuitar
        .getProperty("effects.tremolo-bar.dive"), new BendingEffect());
    this.defaultTremoloBars[1].getTremoloBar().addPoint(0, 0);
    this.defaultTremoloBars[1].getTremoloBar().addPoint(9, -2);
    this.defaultTremoloBars[1].getTremoloBar().addPoint(12, -2);

    this.defaultTremoloBars[2] = new DefaultTremoloBar(TuxGuitar
        .getProperty("effects.tremolo-bar.release-up"), new BendingEffect());
    this.defaultTremoloBars[2].getTremoloBar().addPoint(0, -2);
    this.defaultTremoloBars[2].getTremoloBar().addPoint(9, -2);
    this.defaultTremoloBars[2].getTremoloBar().addPoint(12, 0);

    this.defaultTremoloBars[3] = new DefaultTremoloBar(TuxGuitar
        .getProperty("effects.tremolo-bar.inverted-dip"), new BendingEffect());
    this.defaultTremoloBars[3].getTremoloBar().addPoint(0, 0);
    this.defaultTremoloBars[3].getTremoloBar().addPoint(6, 2);
    this.defaultTremoloBars[3].getTremoloBar().addPoint(12, 0);

    this.defaultTremoloBars[4] = new DefaultTremoloBar(TuxGuitar
        .getProperty("effects.tremolo-bar.return"), new BendingEffect());
    this.defaultTremoloBars[4].getTremoloBar().addPoint(0, 0);
    this.defaultTremoloBars[4].getTremoloBar().addPoint(9, 2);
    this.defaultTremoloBars[4].getTremoloBar().addPoint(12, 2);

    this.defaultTremoloBars[5] = new DefaultTremoloBar(TuxGuitar
        .getProperty("effects.tremolo-bar.release-down"), new BendingEffect());
    this.defaultTremoloBars[5].getTremoloBar().addPoint(0, 2);
    this.defaultTremoloBars[5].getTremoloBar().addPoint(9, 2);
    this.defaultTremoloBars[5].getTremoloBar().addPoint(12, 0);
  }

  private GridData resizeData(GridData data, int minimumWidth, int minimumHeight) {
    data.minimumWidth = minimumWidth;
    data.minimumHeight = minimumHeight;
    return data;
  }

  private void setStyleX(TGPainter painter, int i) {
    painter.setLineStyle(SWT.LINE_SOLID);
    if (i == 0 || i == (X_LENGTH - 1)) {
      painter.setForeground(this.editor.getDisplay().getSystemColor(
          SWT.COLOR_BLACK));
    } else {
      painter.setForeground(this.editor.getDisplay().getSystemColor(
          SWT.COLOR_BLUE));
      if ((i % 3) > 0) {
        painter.setLineStyle(SWT.LINE_DOT);
      }
    }
  }

  private void setStyleY(TGPainter painter, int i) {
    painter.setLineStyle(SWT.LINE_SOLID);
    if (i == 0 || i == (Y_LENGTH - 1)) {
      painter.setForeground(this.editor.getDisplay().getSystemColor(
          SWT.COLOR_BLACK));
    } else if (i == (EffectPoint.MAX_VALUE_LENGTH)) {
      painter.setForeground(this.editor.getDisplay().getSystemColor(
          SWT.COLOR_BLACK));
    } else {
      painter.setForeground(this.editor.getDisplay().getSystemColor(
          SWT.COLOR_RED));
      if ((i % 2) > 0) {
        painter.setLineStyle(SWT.LINE_DOT);
        painter.setForeground(this.editor.getDisplay().getSystemColor(
            SWT.COLOR_GRAY));
      }
    }
  }

  public void setTremoloBar(BendingEffect effect) {
    this.points.clear();
    for (final EffectPoint point : effect.getPoints()) {
      this.makePoint(point);
    }
  }

  public BendingEffect show(Shell shell, final TGNote note) {
    final Shell dialog = DialogUtils.newDialog(shell, SWT.DIALOG_TRIM
        | SWT.APPLICATION_MODAL);

    dialog.setLayout(new GridLayout());
    dialog.setText(TuxGuitar.getProperty("effects.tremolo-bar-editor"));

    // ----------------------------------------------------------------------
    Composite composite = new Composite(dialog, SWT.NONE);
    composite.setLayout(new GridLayout(2, false));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    Composite leftComposite = new Composite(composite, SWT.NONE);
    leftComposite.setLayout(new GridLayout());
    leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    Composite rightComposite = new Composite(composite, SWT.NONE);
    rightComposite.setLayout(new GridLayout());
    rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    // -------------EDITOR---------------------------------------------------
    this.editor = new Composite(leftComposite, SWT.BORDER | SWT.DOUBLE_BUFFERED);
    this.editor.setBackground(this.editor.getDisplay().getSystemColor(
        SWT.COLOR_WHITE));
    this.editor.setLayoutData(resizeData(new GridData(SWT.FILL, SWT.FILL, true,
        true), getWidth() + (X_SPACING * 2), getHeight() + (Y_SPACING * 2)));
    this.editor.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        TGPainter painter = new TGPainter(e.gc);
        paintEditor(painter);
      }
    });
    this.editor.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
        checkPoint(e.x, e.y);
        TremoloBarEditor.this.editor.redraw();
      }
    });

    // -------------DEFAULT BEND
    // LIST---------------------------------------------------
    this.resetDefaultTremoloBars();
    final org.eclipse.swt.widgets.List defaultTremoloBarList = new org.eclipse.swt.widgets.List(
        rightComposite, SWT.BORDER);
    for (int i = 0; i < this.defaultTremoloBars.length; i++) {
      defaultTremoloBarList.add(this.defaultTremoloBars[i].getName());
    }
    defaultTremoloBarList.select(0);
    defaultTremoloBarList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
        true));
    defaultTremoloBarList.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        int index = defaultTremoloBarList.getSelectionIndex();
        if (index >= 0
            && index < TremoloBarEditor.this.defaultTremoloBars.length) {
          setTremoloBar(TremoloBarEditor.this.defaultTremoloBars[index]
              .getTremoloBar());
          TremoloBarEditor.this.editor.redraw();
        }
      }
    });

    // ------------------BUTTONS--------------------------
    Button buttonClean = new Button(rightComposite, SWT.PUSH);
    buttonClean.setLayoutData(resizeData(new GridData(SWT.FILL, SWT.BOTTOM,
        true, true), 80, 25));
    buttonClean.setText(TuxGuitar.getProperty("clean"));
    buttonClean.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        TremoloBarEditor.this.result = null;
        dialog.dispose();
      }
    });
    Button buttonOK = new Button(rightComposite, SWT.PUSH);
    buttonOK.setLayoutData(resizeData(new GridData(SWT.FILL, SWT.BOTTOM, true,
        false), 80, 25));
    buttonOK.setText(TuxGuitar.getProperty("ok"));
    buttonOK.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        TremoloBarEditor.this.result = getTremoloBar();
        dialog.dispose();
      }
    });
    Button buttonCancel = new Button(rightComposite, SWT.PUSH);
    buttonCancel.setLayoutData(resizeData(new GridData(SWT.FILL, SWT.BOTTOM,
        true, false), 80, 25));
    buttonCancel.setText(TuxGuitar.getProperty("cancel"));
    buttonCancel.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        TremoloBarEditor.this.result = note.getEffect().getTremoloBar();
        dialog.dispose();
      }
    });

    if (note.getEffect().isTremoloBar()) {
      setTremoloBar(note.getEffect().getTremoloBar());
    } else {
      setTremoloBar(this.defaultTremoloBars[0].getTremoloBar());
    }

    dialog.setDefaultButton(buttonOK);

    DialogUtils.openDialog(dialog, DialogUtils.OPEN_STYLE_CENTER
        | DialogUtils.OPEN_STYLE_PACK | DialogUtils.OPEN_STYLE_WAIT);

    return this.result;
  }
}
