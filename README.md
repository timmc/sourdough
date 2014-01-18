# sourdough.py

A sourdough versioning program. (Very much still in progress.)

## Goals

- Record modifications to multiple sourdough starters
- Calculate statistics on what flours are present in what ratios
- Record lineage of a starter (parents, culture hosts)
- Represent merging and duplication of starters
- Tag past "revisions" that have been stored in the freezer
- Support both weight and volume measures (not going to try to convert
  between these, though)

## Status

You can record basic data and notes, but no stats are performed.

## Sample usage

```bash
$ mkdir starter && cd starter
$ sourdough.py new gooshy # start a new branch
$ sourdough.py volume 900mL
$ sourdough.py contains tapioca potato quinoa water # past additions
$ sourdough.py add sorghum 250mL water 250mL
$ sourdough.py note 'Starter has started bubbling quite a bit'
$ sourdough.py remove 400mL 1000mL # note the ratio taken:remaining
```

## Warnings

- No molly-guards against editing wrong branch, including
  already-staged edits, etc.
