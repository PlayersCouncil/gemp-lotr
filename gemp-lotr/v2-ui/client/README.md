# LOTR GEMP UI Refresh

This application is a full refresh of the GEMP UI to bring it up to date with modern UI architecture.

# Usage
This app is intended to be standalone, but the API for GEMP still requires/expects that everything is running locally. To account for this the [Vite Config](./vite.config.ts) rewrites all the HTTP Header information to spoof coming from the same localhost port that the GEMP Docker Image serves.

## Prerequisites
* Clone this repo locally
* Clone the [GEMP LOTR repo](https://github.com/PlayersCouncil/gemp-lotr) and follow their setup steps
    * Until you can get to http://localhost:17001/gemp-lotr/ and have it locally work then no steps after here will matter
* Install [Node.js](https://nodejs.org/en). The LTS version (18.14.2) is what I have used, but earlier versions might also work

## Setup
* Open a terminal at the root of this repo.
* Use `npm install` to install all dependencies
* Use `npm run dev`to start the app
* Navigate to http://localhost:5173 (by default, but the terminal will give a link)
* Use the same login info that you have set up while building/hosting the GEMP LOTR app
    * This app is just an alternative UI for the GEMP app that already exists. You're using the same DB and infra that you setup in the Prerequisite section

# Features

[x] Login Page
[x] Basic API Interaction
[] "Hall" Main Page
[] Deck Builder
    [x] Card Display
    [x] Filter Cards + API Integration
    [] Persist Decks
    [] Deck Imports
[] Game History
[] Game UI
[] Leagues
[] Admin

## Login Page
* Simple integration with the API.
* Has some responsive stuff going on to potentially allow better support for small screens

## Basic API
* The GEMP API has a very aggressive refresh every five seconds and this gates the rest of the app. [We integrate with it here](./src/lotr-common/api/hall-api/heartbeat/hall-heartbeat.ts)

## Deck Builder (in progress)
* We gotta display cards
* We gotta filter cards
* I need to come up with the UI/UX for this, but using the same general patterns as MECCG deckbuilder was suggested
[] Allow sorting of the deck contents based on things like twilight cost, name etc
[] determine the best pattern for zooming in on cards
[] add a way to change the card scale size in the "card bank" or deck veiw

## Game History

## Game UI

## Leagues

## Admin

## MUI Notes
* using the box shadows isn't too hard
```      
sx={(theme) => ({
        boxShadow: theme.shadow.lg,
        backgroundColor: 'neutral.dark',
      })}
```