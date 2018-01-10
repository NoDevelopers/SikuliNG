/*
 * Copyright (c) 2017 - sikulix.com - MIT license
 */

package com.sikulix.editor;

import com.sikulix.api.Do;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.KeyEvent;
import java.util.EventObject;

class ScriptTable extends JTable {

  private Script script;

  public ScriptTable(Script script, AbstractTableModel tableModel) {
    super(tableModel);
    this.script = script;
  }

  private static final int numberCol = 0;
  private static final int commandCol = 1;

  @Override
  public boolean editCellAt(int row, int col, EventObject e) {
    ScriptCell currentCell = script.cellAt(row, col);
    int currentRow = row;
    int currentCol = col;
    boolean isLineNumber = currentCol == numberCol;
    boolean isCommand = currentCol == commandCol;
    if (e instanceof KeyEvent) {
      int keyCode = ((KeyEvent) e).getExtendedKeyCode();
      if (keyCode == 0 || keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_META) {
        return false;
      }
      boolean isCtrl = false;
      int modifier = ((KeyEvent) e).getModifiers();
      if (modifier == KeyEvent.CTRL_MASK) {
        isCtrl = true;
      }
      if (isLineNumber) {
        if (keyCode == KeyEvent.VK_PLUS) {
          script.cellAt(row, col).newLine(getSelectedRows());
          new Thread(new Runnable() {
            @Override
            public void run() {
              Do.write("#ESC.");
            }
          }).start();
          return false;
        }
        if (keyCode == KeyEvent.VK_MINUS) {
          script.cellAt(row, col).deleteLine(getSelectedRows());
          new Thread(new Runnable() {
            @Override
            public void run() {
              Do.write("#ESC.");
            }
          }).start();
          return false;
        }
        if (keyCode == KeyEvent.VK_BACK_SPACE) {
          if (isCtrl) {
            script.log.trace("editCellAt: CTRL Backspace");
          } else {
            currentCell.emptyLine(getSelectedRows());
          }
          setSelection(currentRow, commandCol);
          return false;
        }
      }
      if (isCommand && keyCode == KeyEvent.VK_SPACE && currentCell.isEmpty()) {
        script.popUpMenus.command(currentCell);
        return false;
      } else if (keyCode == KeyEvent.VK_SPACE) {
        script.editBox(currentCell);
        return false;
      } else if (keyCode == KeyEvent.VK_BACK_SPACE && currentCell.isEmpty()) {
        if (isCommand) {
          script.setValueAt(script.savedCellText, currentCell);
        } else {
          currentCell.setValue(script.savedCellText);
        }
        return false;
      } else if (keyCode == KeyEvent.VK_F1) {
        script.assist(currentCell);
        return false;
      } else if (keyCode == KeyEvent.VK_F2) {
        Script.log.trace("F2: save script");
        script.saveScript();
        return false;
      } else if (keyCode == KeyEvent.VK_F3) {
        Script.log.trace("F3: open script");
        script.loadScript();
        return false;
      } else if (keyCode == KeyEvent.VK_F4) {
        Script.log.trace("F4: run script");
        if (isLineNumber) {
          script.runScript(-1);
        } else {
          script.runScript(currentRow);
        }
        return false;
      } else if (keyCode == KeyEvent.VK_F5) {
        Script.log.trace("F5: find");
        currentCell.find();
        return false;
      } else if (keyCode == KeyEvent.VK_F6) {
        return false;
      } else if (keyCode == KeyEvent.VK_F7) {
        return false;
      } else if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
        script.savedCellText = currentCell.get();
        currentCell.setValue("");
        return false;
      }
      Script.log.trace("keycode: %d %s", keyCode, KeyEvent.getKeyText(keyCode));
    }
    if (!isLineNumber) {
      return super.editCellAt(currentRow, currentCol, e);
    }
    return false;
  }

  protected void setSelection(int row, int col) {
    setRowSelectionInterval(row, row);
    setColumnSelectionInterval(col, col);
  }

  //TODO correct possible focus problems
  protected void tableHasChanged() {
    setValueAt(null, -1, -1);
  }

  protected void tableCheckContent() {
    script.checkContent();
  }

  protected void resetLineCol() {
    for (int n = 0; n < getModel().getRowCount(); n++) {
      ((ScriptTableModel) getModel()).cellUpdated(n, 0);
    }
  }
}
