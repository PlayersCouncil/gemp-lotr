# Deck Validation

## Overview of Current GEMP Setup
* When a card is added the following API is called:
  * `POST https://play.lotrtcgpc.net/gemp-lotr-server/deck/stats`
    * Headers
      * content-type: `application/x-www-form-urlencoded`
    * Body:
        * participantId (null)
        * targetFormat (e.g. fotr_block)
        * deckContents 
            * `Ringbearer blueprint ID|ring blueprint ID|sites blueprint IDs|all card blueprint IDs including duplciates for each duplicate in the deck`
    * response examples
        * `<b>Free People</b>
            : 61, <b>Shadow</b>
            : 60<br/>
            <b>Fellowship Block</b>
            : 
            <font color='red'>
                Deck contains different number of Shadow and Free peoples cards.<br>
                <br>Deck contains more of the same card than allowed - Arwen (5>4): Arwen
            </font>
            <br/>
            `
        * `<b>Free People</b>
            : 60, <b>Shadow</b>
            : 60<br/>
            <b>Fellowship Block</b>
            : <font color='green'>Valid</font>
            <br/>
            `
* When submitting a deck
  * `POST https://play.lotrtcgpc.net/gemp-lotr-server/deck`
  * Headers
    * content-type: `application/x-www-form-urlencoded`
  * Body
    * participantId: null
    * deckName: test-deck
    * targetFormat: 
    * notes: null
    * deckContents: 
  * Response Examples
    * 200 OK (and then pops a modal)
* Rename a deck
  * `POST https://gemp.starwarsccg.org/gemp-swccg-server/deck/rename`
  * Headers
    * content-type: `application/x-www-form-urlencoded`
  * Body
    * participantId
    * oldDeckName
    * deckName

## How this UI will Handle Deck Validation/Submission
* The Save button is disabled unless validated
* Client side validation (using the validators) on every modification to the deck
* If the client side validation all passes then call `/stats` on every modification to the deck
* Save will call `/deck`