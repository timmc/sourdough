#!/usr/bin/env python

from __future__ import print_function

import sys
import subprocess
from datetime import datetime
import json


def timestamp(whenUTC=None):
  """Produce the current UTC timestamp in ISO 8601 format."""
  return datetime.isoformat(whenUTC or datetime.utcnow()) + 'Z'

def stampedJson(dict):
  """Return a serialized JSON string of the dictionary with a 'when'
  key providing the timestamp string."""
  dict = dict.copy()
  dict['when'] = timestamp()
  return json.dumps(dict, ensure_ascii=False)

def appendHistory(lines, message):
  """Append the list of single lines to the history file and commit
  with the given message."""
  with open('history.log', 'a') as f:
    print("\n".join(lines), file=f)
  # TODO: Bail out if the working dir is unclean
  subprocess.check_call(['git', 'add', 'history.log'])
  subprocess.check_call(['git', 'commit', '-m', message])


# History types: event, state, note, format
# Event types:
# - add: Add a quantity of a substance
# - remove: Remove a quantity, leaving behind a quantity
# Known state types:
# - tsd:volume: Measured quantity (string)
# - tsd:contains: A list of substance names

class History:
  """Formatting for history items. Methods starting with 'f' are
  formatters, taking data and producing a one-line string, and methods
  starting with 'p' are parsers, doing the opposite."""

  @staticmethod
  def comment(msg):
    """Convert a potentially multiline message into a comment, also
    potentially multi-line."""
    return "\n".join(['# ' + l for l in msg.splitlines()])

  @staticmethod
  def fFormat():
    return 'format ' + json.dumps({'spec':'timmc.sourdough', 'version':'1'})

  @staticmethod
  def pFormat(rest):
    return [json.loads(rest)]

  @staticmethod
  def fAdd(substance, quantity, attrs={}):
    data = {'substance':substance, 'qty':quantity, 'attrs':attrs}
    return 'event add ' + stampedJson(data)

  @staticmethod
  def pEvent(rest):
    [subtag, rest] = rest.split(' ', 1)
    return [subtag.strip(), json.loads(rest)]

  @staticmethod
  def fState(name, value):
    return 'state ' + stampedJson({'name':name, 'value':value})

  @staticmethod
  def pState(rest):
    return [json.loads(rest)]

  @staticmethod
  def fNote(message):
    return 'note ' + stampedJson({'text':message})

  @staticmethod
  def pNote(rest):
    return [json.loads(rest)]

  @staticmethod
  def parse(line):
    line = line.strip()
    if line.startswith('#'):
      return None
    pieces = line.split(' ', 1)
    if len(pieces) < 2:
      return ['unknown', rest] # For now there aren't any no-data tags
    [tag, rest] = pieces # assumes at least a piece of data or a sub-tag
    rest = rest.strip()
    tail = None
    if tag == 'event':
      tail = History.pEvent(rest)
    elif tag == 'state':
      tail = History.pState(rest)
    elif tag == 'note':
      tail = History.pNote(rest)
    elif tag == 'format':
      tail = History.pFormat(rest)
    else:
      return ['unknown', rest]
    ret = [tag]
    ret.extend(tail)
    return ret

  @staticmethod
  def parseAll():
    """Return a parsed version of history, as a list of lists. Each
    list contains one or more item tags such as "event" and a final
    element that is parsed from JSON."""
    text = None
    with open('history.log', 'r') as f:
      text = f.read()
    lines = [History.parse(line) for line in text.splitlines()]
    # Filter out all the None responses
    return [line for line in lines if line is not None]


class Stats:
  @staticmethod
  def getName():
    """Get the name of the current line."""
    naming = [entry for entry in History.parseAll()
              if entry[0] == 'state' and entry[1]['name'] == 'tsd:name']
    if len(naming) == 0:
      return None
    else:
      return naming[-1][1]['value']


class Actions:
  @staticmethod
  def usage():
    print("Usage: TODO, read the source for now :-(")

  @staticmethod
  def init():
    """Initialize sourdough tracker in current directory."""
    subprocess.check_call(['git', 'init'])

  @staticmethod
  def new(name):
    """Start new sourdough line, with name and optional quantity."""
    Actions.init()
    # TODO: Prevent smashing of existing branch
    subprocess.check_call(['git', 'checkout', '--orphan', name])
    subprocess.check_call(['git', 'rm', '-rf', '--ignore-unmatch', '.'])
    start = 'Structured history of line named "' + name + '"'
    hist = [History.comment(start),
            History.fFormat(),
            History.fState('tsd:name', name)]
    appendHistory(hist, 'Initializing line')

  @staticmethod
  def volume(total):
    appendHistory([History.fState('tsd:volume', total)],
                  "Noting volume: " + total)

  @staticmethod
  def contains(*substances):
    lines = [History.fState('tsd:contains', substances)]
    msg = 'Noting ingredients\n\n' + ", ".join(substances)
    appendHistory(lines, msg)

  @staticmethod
  def add(*args):
    """Add substance/quantity pairs from alternating list."""
    Actions.init()
    if len(args) % 2 != 0:
      print('Must have even number of substance/quantity arguments (pairs).')
      sys.exit(1)
    pairs = zip(args[::2], args[1::2])
    additions = [History.fAdd(p[0], p[1]) for p in pairs]
    appendHistory(additions, 'Adding ingredients')

  @staticmethod
  def note(message):
    """Add a timestamped note about the line."""
    Actions.init()
    appendHistory([History.fNote(message)], 'Adding a note')

  @staticmethod
  def current():
    name = Stats.getName()
    if name is None:
      print('This line has not been named!')
      sys.exit(1)
    else:
      print(name)


knownActions = ['usage', 'init', 'new', 'volume', 'contains', 'add', 'note',
                'current']

def main(args):
  if len(args) == 0:
    Actions.usage()
  else:
    action = args[0]
    if action not in knownActions:
      print('Unknown action: ' + args[0])
      sys.exit(1)
    method = getattr(Actions, args[0])
    method(*args[1:])


if __name__ == '__main__':
  main(sys.argv[1:])
