package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_301_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frodo", "4_301");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo, Courteous Halfling is the Ring-bearer here.
	protected VirtualTableScenario GetDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "7_16");       // Arwen, Fair Elf Maiden: an unbound companion; her text is about skirmishing a fierce minion
					put("legolas", "1_50");     // Legolas, Greenleaf: an unbound companion; an archer, and no test here reaches the archery phase
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion who is Ring-bound, and so not one of the three
					// Orc Inquisitor: [sauron] Orc, cost 3.  "When you play this minion, you may make the Free Peoples
					// player discard a card at random from hand."  A discard the Free Peoples player has no choice in.
					put("inquisitor", "1_268");
					// Hand of Sauron: [sauron] Maneuver event, cost 0.  "Exert a [sauron] minion to make the Free Peoples
					// player discard a card at random from his or her hand."  A discard that is paid for, so the
					// event is played whatever it will do.
					put("hand", "3_90");
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo, Courteous Halfling is the Ring-bearer here.
	protected VirtualTableScenario GetBarScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, an unbound companion; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf, an unbound companion; an archery ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, an unbound companion; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion, and so not one of the three
					put("bounder", "1_286");    // Bounder: a Hobbit ally, no companion; a skirmish ability only
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, cost 2, site 6, vitality 2; a skirmish ability only
					// Tower of Barad-dur: [sauron] condition.  "Each time the fellowship moves, the Free Peoples player must
					// discard a card from hand": a discard he has no choice in but which card.
					put("tower", "3_104");
					// Desperate Measures: [sauron] Maneuver event.  "Spot a [sauron] Orc and remove a burden to make the Free
					// Peoples player discard top 5 cards from his draw deck."
					put("measures", "1_245");
					// Dunlending Rampager: [dunland] Man, cost 1, site 3.  "When you play this minion, the Free Peoples player
					// may discard 2 cards from hand to discard him."  The card of the ruling.
					put("rampager", "5_1");
					// Search Far and Wide: [dwarven] Fellowship event.  "Spot a Dwarf and discard the top card of your draw deck
					// to draw 2 cards": a Free Peoples card's discard from the top of the deck.
					put("search", "4_56");
					// Enemy Without Number: [uruk-hai] condition.  "Shadow: Discard 2 cards from hand to add (1)": a Shadow
					// card's discard from its own player's hand.
					put("enemy", "13_162");
					// Arwen, Fair Elf Maiden: an [elven] Elf, an unbound companion and no archer; her text is about skirmishing a fierce minion
					put("arwen", "7_16");
					// Uruk-hai Sword: [isengard] hand weapon, a possession.  "Bearer must be an Uruk-hai.  Each time bearer wins a
					// skirmish, the Free Peoples player must discard the top card of his or her draw deck."  A Shadow item.
					put("sword", "1_160");
					// Uruk-hai Band: [isengard] Uruk-hai, strength 10, damage +1 and nothing else (+2 for each site its owner controls: none here)
					put("band", "4_202");
					// Goblin Runners: cards for the draw decks, never played
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	// The same, with Midgewater Moors as site 2: "Each time a minion is played, the Free Peoples player discards a
	// card from hand", a discard made by a site, which is no Shadow card.
	protected VirtualTableScenario GetMoorsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, an unbound companion; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf, an unbound companion; an archery ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, an unbound companion; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion, and so not one of the three
					put("bounder", "1_286");    // Bounder: a Hobbit ally, no companion; a skirmish ability only
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, cost 2, site 6, vitality 2; a skirmish ability only
					// Tower of Barad-dur: [sauron] condition.  "Each time the fellowship moves, the Free Peoples player must
					// discard a card from hand": a discard he has no choice in but which card.
					put("tower", "3_104");
					// Desperate Measures: [sauron] Maneuver event.  "Spot a [sauron] Orc and remove a burden to make the Free
					// Peoples player discard top 5 cards from his draw deck."
					put("measures", "1_245");
					// Dunlending Rampager: [dunland] Man, cost 1, site 3.  "When you play this minion, the Free Peoples player
					// may discard 2 cards from hand to discard him."  The card of the ruling.
					put("rampager", "5_1");
					// Search Far and Wide: [dwarven] Fellowship event.  "Spot a Dwarf and discard the top card of your draw deck
					// to draw 2 cards": a Free Peoples card's discard from the top of the deck.
					put("search", "4_56");
					// Enemy Without Number: [uruk-hai] condition.  "Shadow: Discard 2 cards from hand to add (1)": a Shadow
					// card's discard from its own player's hand.
					put("enemy", "13_162");
					// Goblin Runners: cards for the draw decks, never played
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_333");
				}},
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Frodo, Courteous Halfling
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Signet: Frodo
		 * Ring-bearer: True
		 * Game Text: <b>Ring-bearer (resistance 10).</b><br>While you can spot 3 unbound companions, Shadow cards may not discard cards from your hand or from the top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("frodo");

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Courteous Halfling", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void WithThreeUnboundCompanionsAShadowCardCannotMakeYouDiscardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		// The Free Peoples player's own copy of the Inquisitor is a card in his hand and nothing more.
		var spare = scn.GetFreepsCard("inquisitor");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var hand = scn.GetShadowCard("hand");
		scn.MoveCompanionsToTable(arwen, legolas, aragorn);
		scn.MoveCardsToHand(sam, spare, hand);

		scn.StartGame();
		assertInHand(sam, spare);
		scn.MoveMinionsToTable(inquisitor);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetWoundsOn(inquisitor));

		// Hand of Sauron is an event with a cost: it is played, and paid for, whatever it will do.
		assertTrue(scn.ShadowPlayAvailable(hand));
		scn.ShadowPlayCard(hand);
		assertEquals(1, scn.GetWoundsOn(inquisitor));

		// Arwen, Legolas and Aragorn are three unbound companions: the hand is as it was.
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(sam, spare);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	// The same bar against a "you may" that costs nothing: its whole effect would do nothing, so it is not offered
	// (Q-0034, and Ketura on Q-0231: B).  The engine withholds it, from what the random discard says of itself
	// (Q-0224); WithTwoUnboundCompanionsAndARingBoundOneAShadowCardStillMakesYouDiscardFromHand has the offer.
	@Test
	public void WithThreeUnboundCompanionsAShadowCardsOptionalDiscardFromHandIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var spare = scn.GetFreepsCard("inquisitor");
		var inquisitor = scn.GetShadowCard("inquisitor");
		scn.MoveCompanionsToTable(arwen, legolas, aragorn);
		scn.MoveCardsToHand(sam, spare, inquisitor);

		scn.StartGame();
		assertInHand(sam, spare);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());

		scn.ShadowPlayCard(inquisitor);

		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(sam, spare);
	}

	@Test
	public void WithTwoUnboundCompanionsAndARingBoundOneAShadowCardStillMakesYouDiscardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var spare = scn.GetFreepsCard("inquisitor");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var hand = scn.GetShadowCard("hand");
		// Three companions beside Frodo again, but Sam is Ring-bound: two unbound companions, one short.
		scn.MoveCompanionsToTable(arwen, legolas, sam);
		scn.MoveCardsToHand(aragorn, spare, inquisitor, hand);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(inquisitor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		scn.ShadowAcceptOptionalTrigger();

		// One of the two cards, at random, has gone to the discard pile.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Hand of Sauron, the event that WithThreeUnboundCompanionsAShadowCardCannotMakeYouDiscardFromHand plays under the
		// bar, discards the last card at random when the bar is not there.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(inquisitor));
		scn.ShadowPlayCard(hand);
		assertEquals(1, scn.GetWoundsOn(inquisitor));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithThreeUnboundCompanionsAShadowCardDiscardsNothingFromTheTopOfYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBarScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var soldier = scn.GetShadowCard("soldier");
		var measures = scn.GetShadowCard("measures");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveCardsToHand(boromir, measures);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		// The third unbound companion is played: the count is reached by play, ahead of the act it bars.
		assertInHand(boromir);
		scn.FreepsPlayCard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		scn.FreepsPass();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 5);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetBurdens());

		// Desperate Measures is an event with a cost: it is played, and paid for, whatever it will do.
		// Gimli, Legolas and Boromir are three unbound companions: the draw deck is as it was.
		assertTrue(scn.ShadowPlayAvailable(measures));
		scn.ShadowPlayCard(measures);
		assertEquals(0, scn.GetBurdens());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	// Uruk-hai Band, with the Sword, is strength 10 + 2 against Boromir's 7 and damage +1: it wins, and wounds him twice.
	@Test
	public void WithThreeUnboundCompanionsAShadowPossessionDiscardsNothingFromTheTopOfYourDrawDeckWhenItsBearerWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBarScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var band = scn.GetShadowCard("band");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCompanionsToTable(gimli, arwen, boromir);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(band);
		scn.AttachCardsTo(band, sword);
		assertAttachedTo(sword, band);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, band);
		scn.FreepsResolveSkirmish(boromir);

		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 1);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPass();
		scn.ShadowPass();
		// The wound for losing and the Sword's trigger come at the same moment.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Uruk-hai Sword");

		// Boromir lost: the Sword's bearer has won, and the three unbound companions keep the top of the deck as it was.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoUnboundCompanionsAShadowPossessionDiscardsTheTopCardOfYourDrawDeckWhenItsBearerWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBarScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var band = scn.GetShadowCard("band");
		var sword = scn.GetShadowCard("sword");
		// Two unbound companions, one short, beside a Ring-bound one.
		scn.MoveCompanionsToTable(boromir, arwen, sam);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(band);
		scn.AttachCardsTo(band, sword);
		assertAttachedTo(sword, band);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, band);
		scn.FreepsResolveSkirmish(boromir);

		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 1);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPass();
		scn.ShadowPass();
		// The wound for losing and the Sword's trigger come at the same moment.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Uruk-hai Sword");

		// Boromir (7) lost to the Band (10 + 2): the bearer has won, and the top card of the deck is discarded.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	@Ignore("F-0514: a Shadow card's forced discard from hand under the bar still asks the Free Peoples player to choose a card, then discards none")
	public void WithThreeUnboundCompanionsAShadowCardMakesYouChooseNoCardFromHandAndDiscardsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBarScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		// The Free Peoples player's own copies are cards in his hand and nothing more.
		var spare1 = scn.GetFreepsCard("soldier");
		var spare2 = scn.GetFreepsCard("measures");
		var tower = scn.GetShadowCard("tower");
		scn.MoveCompanionsToTable(gimli, legolas, boromir);
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToHand(spare1, spare2);

		scn.StartGame();
		assertInHand(spare1, spare2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The move sets off the Tower: with three unbound companions no card can be discarded, so none is asked for.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(spare1, spare2);
		assertEquals(0, scn.GetFreepsDiscardCount());
	}

	@Test
	public void WithTwoUnboundCompanionsAShadowCardMakesYouDiscardAChosenCardFromHandAndTheTopOfYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBarScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var spare1 = scn.GetFreepsCard("soldier");
		var spare2 = scn.GetFreepsCard("measures");
		// The other player's unbound companion, in his own fellowship: not one of "your" companions to spot.
		var shadowBoromir = scn.GetShadowCard("boromir");
		var tower = scn.GetShadowCard("tower");
		var soldier = scn.GetShadowCard("soldier");
		var measures = scn.GetShadowCard("measures");
		// Two unbound companions, one short, beside a Ring-bound companion, an ally and the other player's companion.
		scn.MoveCompanionsToTable(gimli, legolas, sam, shadowBoromir);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToHand(spare1, spare2, measures);

		scn.StartGame();
		scn.AddBurdens(1);
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 5);
		assertEquals(1, scn.GetBurdens());
		assertInHand(spare1, spare2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The move sets off the Tower, and the Free Peoples player chooses which card he discards.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasCardChoiceAvailable(spare1, spare2));
		scn.FreepsChooseCard(spare1);
		assertInZone(Zone.DISCARD, spare1);
		assertInHand(spare2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(measures);
		assertEquals(0, scn.GetBurdens());
		assertEquals(deck - 5, scn.GetFreepsDeckCount());
		// the Tower's card and the five from the deck
		assertEquals(6, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithThreeUnboundCompanionsAFreePeoplesCardASiteAndAShadowCardDiscardingFromItsOwnPlayersHandStillDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMoorsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var search = scn.GetFreepsCard("search");
		var spare1 = scn.GetFreepsCard("soldier");
		var spare2 = scn.GetFreepsCard("measures");
		var top = scn.GetFreepsCard("rampager");
		var enemy = scn.GetShadowCard("enemy");
		var soldier = scn.GetShadowCard("soldier");
		var shadowTower = scn.GetShadowCard("tower");
		var shadowMeasures = scn.GetShadowCard("measures");
		scn.MoveCompanionsToTable(gimli, legolas, boromir);
		scn.MoveCardsToSupportArea(enemy);
		scn.MoveCardsToHand(search, spare1, spare2);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(top);
		scn.SetShadowHand(soldier, shadowTower, shadowMeasures);
		int deck = scn.GetFreepsDeckCount();
		assertInZone(Zone.DECK, top);
		assertInHand(search, spare1, spare2);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// A Free Peoples card discards the top card of the draw deck to pay for itself.
		scn.FreepsPlayCard(search);
		assertInZone(Zone.DISCARD, top, search);
		assertEquals(2, scn.GetFreepsDiscardCount());
		// one discarded, two drawn
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsHandCount());

		scn.SetTwilight(5);
		scn.FreepsPass();
		assertEquals("Midgewater Moors", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Shadow card discards from the Shadow player's own hand: that is not "your" hand.
		int twilight = scn.GetTwilight();
		scn.ShadowUseCardAction(enemy);
		scn.ShadowChooseCards(shadowTower, shadowMeasures);
		assertInZone(Zone.DISCARD, shadowTower, shadowMeasures);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(twilight + 1, scn.GetTwilight());

		// A minion is played, and the site makes the Free Peoples player discard: a site is no Shadow card.
		scn.ShadowPlayCard(soldier);
		assertTrue(scn.FreepsHasCardChoiceAvailable(spare1, spare2));
		scn.FreepsChooseCard(spare1);
		assertInZone(Zone.DISCARD, spare1);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeUnboundCompanionsTheFreePeoplesPlayerMayStillChooseToDiscardForDunlendingRampager() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBarScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var spare1 = scn.GetFreepsCard("soldier");
		var spare2 = scn.GetFreepsCard("measures");
		var spare3 = scn.GetFreepsCard("tower");
		var rampager = scn.GetShadowCard("rampager");
		scn.MoveCompanionsToTable(gimli, legolas, boromir);
		scn.MoveCardsToHand(spare1, spare2, spare3, rampager);

		scn.StartGame();
		assertInHand(spare1, spare2, spare3);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The Rampager's text gives the Free Peoples player a choice, so Frodo's text does not keep him from it.
		scn.ShadowPlayCard(rampager);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(spare1, spare2, spare3));
		scn.FreepsChooseCards(spare1, spare2);

		assertInZone(Zone.DISCARD, spare1, spare2, rampager);
		assertInHand(spare3);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
