// SPDX-FileCopyrightText: 2026 macmarrum (at) outlook (dot) ie
// SPDX-License-Identifier: GPL-3.0-or-later

import './setup.ts';
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { MindMap } from '../src/index.ts';
import { editedText, shownText } from '../src/map.ts';

test('editedText strips leading newlines but keeps leading spaces, as Freeplane does', () => {
  assert.equal(editedText('\n\n  text'), '  text');
  assert.equal(editedText('\n \ntext'), ' \ntext'); // only the newlines at the very start go
  assert.equal(editedText('   text'), '   text');
});

test('editedText strips trailing spaces and newlines, and nothing else', () => {
  assert.equal(editedText('text \n\n'), 'text');
  assert.equal(editedText('text   '), 'text');
  assert.equal(editedText('line one  \nline two  '), 'line one  \nline two');
});

test('editedText keeps spaces and newlines in the middle of the text', () => {
  assert.equal(editedText('a  b\n\n\nc'), 'a  b\n\n\nc');
});

test('editedText of a text of only newlines is empty', () => {
  assert.equal(editedText('\n\n'), '');
  assert.equal(editedText(''), '');
});

test('editedText keeps the spaces of a text of only spaces and newlines, which Freeplane counts as leading', () => {
  assert.equal(editedText('   '), '   ');
  assert.equal(editedText('  \n\n '), '  \n\n ');
});

test('shownText gives a text ending with newlines one more line, as the editor shows it', () => {
  assert.equal(shownText('a\n'), 'a\n\u200b');
  assert.equal(shownText('a\n\n'), 'a\n\n\u200b');
});

test('shownText leaves other texts as they are', () => {
  assert.equal(shownText('a'), 'a');
  assert.equal(shownText('a\nb'), 'a\nb');
  assert.equal(shownText('  a  '), '  a  ');
  assert.equal(shownText(''), '');
});

const ROOT = '<map version="freeplane 1.12.15">\n<node TEXT="root" ID="ID_1" CREATED="1" MODIFIED="1">\n' +
  '<node TEXT="old" ID="ID_2" CREATED="1" MODIFIED="1"/>\n</node>\n</map>';

test('updateText keeps leading spaces and drops trailing spaces and newlines in the stored text', () => {
  const map = new MindMap(ROOT);
  assert.equal(map.updateText('ID_2', '  first\nsecond \n\n', 2), true);
  assert.match(map.serialize(), /TEXT="  first&#xa;second"/);
});

test('updateText of a text that only differs by trailing newlines changes nothing', () => {
  const map = new MindMap(ROOT);
  assert.equal(map.updateText('ID_2', 'old\n\n', 2), false);
  assert.equal(map.serialize(), ROOT);
});

test('a new node keeps only what editedText keeps', () => {
  const map = new MindMap(ROOT);
  map.addNode('ID_1', 1, '\nnew text\n', 'right', 3);
  assert.match(map.serialize(), /<node TEXT="new text" POSITION="bottom_or_right" ID="ID_\d+"/);
});
