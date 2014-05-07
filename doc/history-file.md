# History file format

A history file is a series of lines that are either comments (lines
where the first non-whitespace character is a `#`) or log entries. Log
entries are JSON arrays consisting of a sequence of strings (this
sequence being called a "tag") followed optionally by some other data
structure (called the "payload".) For instance, an entry might take
the form of:

```json
["event", "change", "environment", "temperature", {"scale": "C", "value": 25}]
```

This entry starts with a sequence of 4 tokens indicating what the
payload represents. Note that the payload may be a string or there may
even be no payload; a program that does not understand the full tag is
incapable of determining whether there is a payload, or even where the
tag ends!

While it is recommended that specifications avoid starting any tag
token with characters that can start a JSON string
(`0123456789-"[{`) or using the tokens `true`, `false`, and `null`,
programs should not rely on this property to create shorthand formats
that drop the quotes. Such a shorthand format is likely to break.

Comments are not expected to be read programmatically; they should be
preserved by default, but are intended as out-of-band notes to
developers and data archaeologists.

The file is to be encoded in UTF-8 with no Byte Order
Mark (BOM). Ideally, NFD normalization form should be used.

## Data formats

Time stamps are represented as strings in ISO 8601 format, including
time zone. They will usually be present under a `"when"` key in a
payload.

## Known tags

The first token of a tag should be one of the following; the
remaining tokens in the entry are parsed differently for each.
Examples are provided to illustrate uses of each entry.

### `format`

The first entry should begin with a `format` tag to indicate the
schema of the history file.

```json
["format", {"spec":"timmc.sourdough", "version":"1"}
```

The payload has at least keys `spec` (specification) and `version`,
both strings. A program that does not recognize the specification
value or only knows about lower versions should not modify the file,
and may not wish to even interpret it.

### `event`

Event tags indicate a transition point in some property and
canonically carry a timestamp to indicate when it happened. Known
event subtypes:

```json
["event", "add", {"substance":"millet", "quantity":"250mL", "when":"2014-01-18T23:01:11.506490Z"}]
```

Add a quantity of a substance: The payload is a map of keys
`substance`, `quantity`, and optionally `when`.

```json
["event", "remove", {"quantity":"550 mL", "when":"2014-01-18T23:01:11.506490Z"}]
```

Remove a quantity. The payload is a map of `quantity` and optionally
`when`.

### `state`

State tags indicate the value of a property at a point in time, but do
not specify that there has been a recent *change* in this property.

```json
["state", "measure", {"metric":"volume", "value":"550 mL"}]
```

A measurement of some property of the starter. Payload is a map with
keys `metric` (known values: `volume`, `mass`) and `value` (a
unit-bearing quantity.) The value is not permitted to be `"?"`.
Payload may bear `when` timestamp.

```json
["state", "describe", "tsd:name", {"value": "Yeastville"}]
```

The name assigned to the starter. Payload is a map of `value` and
optionally `when`.

### `note`

Note tags carry a natural-language message as a payload.

```json
["note", {"value":"The sourdough has not been bubbling much today."}]
```

The paylaod is a map of `value` (carrying the message string) and
optionally a `when` timestamp.
