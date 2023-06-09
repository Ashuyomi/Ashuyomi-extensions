## 1.4.47

Minimum Komga version required: `0.151.0`

### Feat

* add support for AVIF and HEIF image types

## 1.4.46

Minimum Komga version required: `0.151.0`

### Feat

* Update to extension-lib 1.4
  - Clicking on chapter WebView should now open the chapter/book page.

## 1.3.45

Minimum Komga version required: `0.151.0`

### Feat

* Edit source display name

## 1.3.44

Minimum Komga version required: `0.151.0`

### Fix

* Better date/time parsing

## 1.3.43

Minimum Komga version required: `0.151.0`

### Fix

* Requests failing if address preference is saved with a trailing slash

### Features

* Add URL validation in the address preferences
* Use a URL-focused keyboard when available while editing the address preferences

## 1.3.42

Minimum Komga version required: `0.151.0`

### Fix

* default sort broken since Komga 0.155.1
* proper sort criteria for readlists

## 1.3.41

Minimum Komga version required: `0.151.0`

### Features

* Improve how the status is displayed

## 1.3.40

Minimum Komga version required: `0.151.0`

### Features

* Exclude from bulk update warnings

## 1.2.39

Minimum Komga version required: `0.151.0`

### Features

* Prepend series name in front of books within readlists

## 1.2.38

Minimum Komga version required: `0.113.0`

### Features

* Add `README.md`

## 1.2.37

Minimum Komga version required: `0.113.0`

### Features

* In app link to `CHANGELOG.md`

## 1.2.36

Minimum Komga version required: `0.113.0`

### Features

* Don't request conversion for JPEG XL images

## 1.2.35

Minimum Komga version required: `0.113.0`

### Features

* Display the Translators of a book in the scanlator chapter field

## 1.2.34

Minimum Komga version required: `0.113.0`

### Fix

* Loading of filter values could fail in some cases

## 1.2.33

Minimum Komga version required: `0.113.0`

### Fix

* Open in WebView and Share options now open regular browser link instead of showing JSON
* Note that Komga cannot be viewed using System WebView since there is no login prompt
  However, opening in a regular browser works.

## 1.2.32

Minimum Komga version required: `0.113.0`

### Fix

* Source language, conventionally set to "en", is now changed to "all"
* Downloaded files, if any, will have to be moved to new location
    - `Komga (EN)` to `Komga (ALL)`
    - `Komga (3) (EN)` to `Komga (3) (ALL)`

## 1.2.31

Minimum Komga version required: `0.113.0`

### Refactor

* replace Gson with kotlinx.serialization 

## 1.2.30

Minimum Komga version required: `0.113.0`

### Features

* display read list summary
* display aggregated tags on series
* search series by book tags

## 1.2.29

Minimum Komga version required: `0.97.0`

### Features

* filter deleted series and books

## 1.2.28

Minimum Komga version required: `0.97.0`

### Fix

* incorrect User Agent

## 1.2.27

Minimum Komga version required: `0.97.0`

### Fix

* filter series by read or in progress

## 1.2.26

Minimum Komga version required: `0.87.4`

### Fix

* show series with only in progress books when searching for unread only

## 1.2.25

Minimum Komga version required: `0.87.4`

### Fix

* sort order for read list books

## 1.2.24

Minimum Komga version required: `0.87.4`

### Fix

* only show series tags in the filter panel
* set URL properly on series and read lists, so restoring from a backup can work properly


## 1.2.23

Minimum Komga version required: `0.75.0`

### Features

* ignore DNS over HTTPS so it can reach IP addresses

## 1.2.22

Minimum Komga version required: `0.75.0`

### Features

* add error logs and better catch exceptions

## 1.2.21

Minimum Komga version required: `0.75.0`

### Features

* browse read lists (from the filter menu)
* filter by collection, respecting the collection's ordering

## 1.2.20

Minimum Komga version required: `0.75.0`

### Features

* filter by authors, grouped by role

## 1.2.19

Minimum Komga version required: `0.68.0`

### Features

* display Series authors
* display Series summary from books if no summary exists for Series

## 1.2.18

Minimum Komga version required: `0.63.2`

### Fix

* use metadata.releaseDate or fileLastModified for chapter date

## 1.2.17

Minimum Komga version required: `0.63.2`

### Fix

* list of collections for filtering could be empty in some conditions

## 1.2.16

Minimum Komga version required: `0.59.0`

### Features

* filter by genres, tags and publishers

## 1.2.15

Minimum Komga version required: `0.56.0`

### Features

* remove the 1000 chapters limit
* display series description and tags (genres + tags)

## 1.2.14

Minimum Komga version required: `0.41.0`

### Features

* change chapter display name to use the display number instead of the sort number

## 1.2.13

Minimum Komga version required: `0.41.0`

### Features

* compatibility for the upcoming version of Komga which have changes in the API (IDs are String instead of Long)

## 1.2.12

Minimum Komga version required: `0.41.0`

### Features

* filter by collection

## 1.2.11

Minimum Komga version required: `0.35.2`

### Features

* Set password preferences inputTypes

## 1.2.10

Minimum Komga version required: `0.35.2`

### Features

* unread only filter (closes gotson/komga#180)
* prefix book titles with number (closes gotson/komga#169)

## 1.2.9

Minimum Komga version required: `0.22.0`

### Features

* use SourceFactory to have multiple Komga servers (3 for the moment)

## 1.2.8

Minimum Komga version required: `0.22.0`

### Features

* use book metadata title for chapter display name
* use book metadata sort number for chapter number

## 1.2.7

### Features

* use series metadata title for display name
* filter on series status

## 1.2.6

### Features

* Add support for AndroidX preferences 

## 1.2.5

### Features

* add sort options in filter

## 1.2.4

### Features

* better handling of authentication

## 1.2.3

### Features

* filters by library

## 1.2.2

### Features

* request converted image from server if format is not supported

## 1.2.1

### Features

* first version
