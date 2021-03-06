package org.herac.tuxguitar.gui.undo.undoables.measure;

import java.util.ArrayList;
import java.util.List;

import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.undo.CannotRedoException;
import org.herac.tuxguitar.gui.undo.CannotUndoException;
import org.herac.tuxguitar.gui.undo.UndoableEdit;
import org.herac.tuxguitar.gui.undo.undoables.UndoableCaretHelper;
import org.herac.tuxguitar.song.helpers.TGSongSegment;
import org.herac.tuxguitar.song.helpers.TGSongSegmentHelper;
import org.herac.tuxguitar.song.models.TGMarker;

public class UndoableRemoveMeasure implements UndoableEdit {
  private class UndoMarkers {
    private List<TGMarker> markers;

    public UndoMarkers() {
      this.markers = new ArrayList<TGMarker>();
      for (final TGMarker marker : TuxGuitar.instance().getSongManager()
          .getMarkers()) {
        this.markers.add(marker.clone());
      }
    }

    public void undo() {
      TuxGuitar.instance().getSongManager().removeAllMarkers();

      for (final TGMarker marker : this.markers) {
        TuxGuitar.instance().getSongManager().updateMarker(marker.clone());
      }
    }
  }

  private int doAction;
  private int n1;
  private int n2;
  private UndoableCaretHelper redoCaret;
  private TGSongSegment tracksMeasures;
  private UndoableCaretHelper undoCaret;

  private UndoMarkers undoMarkers;

  public UndoableRemoveMeasure(int n1, int n2) {
    this.doAction = UNDO_ACTION;
    this.undoCaret = new UndoableCaretHelper();
    this.n1 = n1;
    this.n2 = n2;
    this.tracksMeasures = new TGSongSegmentHelper(TuxGuitar.instance()
        .getSongManager()).copyMeasures(n1, n2);
    this.undoMarkers = new UndoMarkers();
  }

  public boolean canRedo() {
    return (this.doAction == REDO_ACTION);
  }

  public boolean canUndo() {
    return (this.doAction == UNDO_ACTION);
  }

  public UndoableRemoveMeasure endUndo() {
    this.redoCaret = new UndoableCaretHelper();
    return this;
  }

  public void redo() throws CannotRedoException {
    if (!canRedo()) {
      throw new CannotRedoException();
    }
    TuxGuitar.instance().getSongManager()
        .removeMeasureHeaders(this.n1, this.n2);
    TuxGuitar.instance().fireUpdate();

    this.redoCaret.update();
    this.doAction = UNDO_ACTION;
  }

  public void undo() throws CannotUndoException {
    if (!canUndo()) {
      throw new CannotUndoException();
    }
    new TGSongSegmentHelper(TuxGuitar.instance().getSongManager())
        .insertMeasures(this.tracksMeasures.clone(), this.n1, 0, 0);

    TuxGuitar.instance().fireUpdate();
    this.undoMarkers.undo();
    this.undoCaret.update();

    this.doAction = REDO_ACTION;
  }
}
