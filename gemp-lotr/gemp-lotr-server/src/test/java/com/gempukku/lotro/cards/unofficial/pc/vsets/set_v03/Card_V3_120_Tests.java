package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_120_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.FOTRFrodo);
	}

	// Gimli, Bearer of Grudges: a Ring-bearer who is no Hobbit; his text is about skirmishes involving him
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.GimliRB);
	}

	protected VirtualTableScenario GetScenario(String ringBearer) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wff", "103_120");
					put("sam", "1_311");        // Sam, Son of Hamfast: Hobbit companion, vitality 4; a fellowship ability never used here
					put("merry", "1_302");      // Merry, Friend to Sam: Hobbit companion, vitality 4, cost 1
					put("pippin", "1_306");     // Pippin, Friend to Frodo: Hobbit companion, vitality 4; his text is about tales
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, vitality 4; a maneuver ability only
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion with no race; an assignment ability only
					put("bounder", "1_286");    // Bounder: a Hobbit ally, vitality 2; a skirmish ability only
					put("longbottom", "1_300"); // Longbottom Leaf: a Free Peoples possession in the support area; a when-played trigger only
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession with no text

					put("armory", "1_173");     // Goblin Armory: [moria] condition, support area; answers [moria] weapons and wounds to [moria] Orcs
					put("armory2", "1_173");
					put("servants", "2_77");    // His Terrible Servants: [wraith] condition, support area; a response about twilight Nazgul
					put("hides", "4_19");       // Hides: [dunland] possession, support area; a when-played trigger and a response about [dunland] Men
					put("weary1", "4_212");     // Weary: [isengard] condition borne by a companion or ally; its text is about [isengard] trackers
					put("weary2", "4_212");
					put("weary3", "4_212");
					put("tales", "6_75");       // Twisted Tales: [isengard] condition, support area; possessions of beaten characters are stacked on it
					put("runner", "1_178");     // Goblin Runner: a minion
				}},
				VirtualTableScenario.FellowshipSites,
				ringBearer,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WorthFightingForStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Worth Fighting For
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: To play, spot a Hobbit companion.
		 * 	Make the Shadow player exhaust 1 of your characters.  For each exertion made, hinder a Shadow support card.
		 * 	If you cannot spot more than 4 companions, you may make the Shadow player shuffle one of their hindered
		 * 	cards (and all cards stacked on it) into their owner's draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wff");

		assertEquals("Worth Fighting For", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAHobbitCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var wff = scn.GetFreepsCard("wff");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");

		// The Ring-bearer is a Dwarf. Beside him: a [shire] companion who is no Hobbit, a Hobbit who is an ally,
		// and a Hobbit companion who is hindered.
		scn.MoveCompanionsToTable(smeagol, pippin);
		scn.MoveAlliesToTable(bounder);
		scn.HinderCard(pippin);
		scn.MoveCardsToHand(wff, merry);

		scn.StartGame();

		assertTrue(scn.IsHindered(pippin));
		assertFalse(scn.FreepsPlayAvailable(wff));

		scn.FreepsPlayCard(merry);

		assertTrue(scn.FreepsPlayAvailable(wff));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowPlayerExhaustsACharacterAndACardIsHinderedForEachExertionThenAHinderedCardIsShuffledIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wff = scn.GetFreepsCard("wff");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var longbottom = scn.GetFreepsCard("longbottom");
		var armory = scn.GetShadowCard("armory");
		var hinderedArmory = scn.GetShadowCard("armory2");
		var servants = scn.GetShadowCard("servants");
		var hides = scn.GetShadowCard("hides");
		var wearyOnAlly = scn.GetShadowCard("weary1");
		var wearyOnSam = scn.GetShadowCard("weary2");
		var hinderedWearyOnFrodo = scn.GetShadowCard("weary3");
		var runner = scn.GetShadowCard("runner");

		// Exactly 4 companions that can be spotted; a fifth is hindered, and there is an ally
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(longbottom);
		scn.AddWoundsToChar(merry, 3);
		scn.MoveCardsToHand(wff);

		scn.MoveCardsToSupportArea(armory, hinderedArmory, servants, hides);
		scn.AttachCardsTo(bounder, wearyOnAlly);
		scn.AttachCardsTo(sam, wearyOnSam);
		scn.AttachCardsTo(frodo, hinderedWearyOnFrodo);
		scn.HinderCard(pippin, hinderedArmory, hinderedWearyOnFrodo);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsRefreshDecision();

		assertTrue(scn.IsHindered(pippin));
		assertTrue(scn.IsHindered(hinderedArmory));
		assertTrue(scn.IsHindered(hinderedWearyOnFrodo));
		assertTrue(scn.IsExhausted(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(wff));
		scn.FreepsPlayCard(wff);

		// The Shadow player chooses: the Ring-bearer, a Ring-bound Hobbit, a Man and an ally are offered
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, aragorn, bounder));
		// An exhausted companion, a hindered companion, and the Shadow player's own character
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(merry, pippin, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(aragorn);

		// Vitality 4: 3 exertions
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// First of 3: the Free Peoples player chooses among two conditions and a possession in the Shadow
		// support area and a condition an ally bears
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, servants, hides, wearyOnAlly));
		// A Shadow condition a companion bears, a minion, a Free Peoples support card, and Shadow cards
		// already hindered
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wearyOnSam, runner, longbottom, hinderedArmory, hinderedWearyOnFrodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(hides);
		assertTrue(scn.IsHindered(hides));

		// Second
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, servants, wearyOnAlly));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hides));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(wearyOnAlly);
		assertTrue(scn.IsHindered(wearyOnAlly));

		// Third and last
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, servants));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wearyOnAlly));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(servants);
		assertTrue(scn.IsHindered(servants));

		// 3 exertions, 3 cards: the fourth is left alone, and so is everything that is no Shadow support card
		assertFalse(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(wearyOnSam));
		assertFalse(scn.IsHindered(runner));
		assertFalse(scn.IsHindered(longbottom));

		// No more than 4 companions can be spotted. "You may": the Free Peoples player is asked first, and
		// the Shadow player has nothing to choose until he answers
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsAnyDecisionsAvailable());
		scn.FreepsChooseYes();

		// The Shadow player chooses one of his hindered cards: the three just hindered, and the two hindered
		// before, one of them no support card
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(hides, wearyOnAlly, servants, hinderedArmory, hinderedWearyOnFrodo));
		// Shadow cards that are not hindered, and a hindered Free Peoples card
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armory, wearyOnSam, runner, pippin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(hinderedWearyOnFrodo);

		assertInZone(Zone.DECK, hinderedWearyOnFrodo);
		assertNotAttachedTo(hinderedWearyOnFrodo, frodo);
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());

		// The others stay where they were, hindered
		assertInZone(Zone.SUPPORT, hides, servants, hinderedArmory);
		assertAttachedTo(wearyOnAlly, bounder);
		assertTrue(scn.IsHindered(hides));
		assertTrue(scn.IsHindered(servants));
		assertTrue(scn.IsHindered(hinderedArmory));
		assertTrue(scn.IsHindered(wearyOnAlly));

		assertInDiscard(wff);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OneExertionHindersOneCardAndWithFiveCompanionsNothingIsShuffledIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wff = scn.GetFreepsCard("wff");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory = scn.GetShadowCard("armory");
		var servants = scn.GetShadowCard("servants");
		var hides = scn.GetShadowCard("hides");

		// 5 companions that can be spotted
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.AddWoundsToChar(merry, 2);
		scn.MoveCardsToHand(wff);
		scn.MoveCardsToSupportArea(armory, servants, hides);

		scn.StartGame();

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(wff);

		// Merry has vitality 4 and 2 wounds: 1 exertion
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, merry, pippin, aragorn));
		scn.ShadowChooseCard(merry);
		assertEquals(3, scn.GetWoundsOn(merry));

		// One card and no more
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, servants, hides));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(servants);

		assertTrue(scn.IsHindered(servants));
		assertFalse(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(hides));

		// More than 4 companions: the hindered card stays, and nobody is asked anything
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.SUPPORT, servants);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInDiscard(wff);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNobodyAbleToExertItIsStillPlayedAndNothingIsHinderedOrShuffledIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wff = scn.GetFreepsCard("wff");
		var frodo = scn.GetRingBearer();
		var armory = scn.GetShadowCard("armory");
		var servants = scn.GetShadowCard("servants");

		// Frodo alone, exhausted
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveCardsToHand(wff);
		scn.MoveCardsToSupportArea(armory, servants);

		scn.StartGame();

		assertTrue(scn.IsExhausted(frodo));
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(wff));
		scn.FreepsPlayCard(wff);

		// Nobody exerts, so nothing is hindered; the Shadow player has no hindered card, so nobody is asked
		// about one and no deck is shuffled
		assertInDiscard(wff);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertFalse(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(servants));
		assertInZone(Zone.SUPPORT, armory, servants);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheCardShuffledInTakesTheCardsStackedOnItIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wff = scn.GetFreepsCard("wff");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var tales = scn.GetShadowCard("tales");
		var armory = scn.GetShadowCard("armory");
		var servants = scn.GetShadowCard("servants");
		var stackedRunner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(wff);
		scn.MoveCardsToSupportArea(tales, armory, servants);
		scn.StackCardsOn(tales, stackedRunner);

		scn.StartGame();

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertInZone(Zone.STACKED, stackedRunner);

		scn.FreepsPlayCard(wff);
		scn.ShadowChooseCard(sam);
		assertEquals(3, scn.GetWoundsOn(sam));

		// 3 exertions and 3 Shadow support cards: the third is the only one left
		scn.FreepsChooseCard(tales);
		scn.FreepsChooseCard(armory);
		assertTrue(scn.IsHindered(tales));
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(servants));
		// Hindering a card does nothing to what is stacked on it
		assertInZone(Zone.STACKED, stackedRunner);

		scn.FreepsChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(tales, armory, servants));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(stackedRunner));
		scn.ShadowChooseCard(tales);

		// The condition and the minion stacked on it
		assertInZone(Zone.DECK, tales, stackedRunner);
		assertEquals(shadowDeck + 2, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());

		assertInZone(Zone.SUPPORT, armory, servants);
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(servants));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFreePeoplesCardStackedOnTheCardShuffledInGoesIntoItsOwnersDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wff = scn.GetFreepsCard("wff");
		var merry = scn.GetFreepsCard("merry");
		var stackedSword = scn.GetFreepsCard("sword");
		var tales = scn.GetShadowCard("tales");
		var stackedRunner = scn.GetShadowCard("runner");

		// Twisted Tales stacks the possessions of characters beaten by [isengard] Men: here it holds the Free
		// Peoples player's Hobbit Sword and a minion of its owner's
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(wff);
		scn.MoveCardsToSupportArea(tales);
		scn.StackCardsOn(tales, stackedSword, stackedRunner);

		scn.StartGame();

		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertInZone(Zone.STACKED, stackedSword, stackedRunner);

		scn.FreepsPlayCard(wff);
		scn.ShadowChooseCard(merry);

		// Twisted Tales is the only Shadow support card and then the only hindered card
		scn.FreepsChooseYes();
		assertInZone(Zone.DECK, tales, stackedRunner, stackedSword);
		// The condition and the minion into the Shadow player's deck, the sword into the Free Peoples player's
		assertEquals(shadowDeck + 2, scn.GetShadowDeckCount());
		assertEquals(freepsDeck + 1, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShuffleIsTheFreePeoplesPlayersToDeclineAndDecliningLeavesTheHinderedCardsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wff = scn.GetFreepsCard("wff");
		var merry = scn.GetFreepsCard("merry");
		var armory = scn.GetShadowCard("armory");
		var servants = scn.GetShadowCard("servants");

		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(wff);
		scn.MoveCardsToSupportArea(armory, servants);

		scn.StartGame();

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(wff);
		scn.ShadowChooseCard(merry);
		// 3 exertions and 2 Shadow support cards: both are hindered
		scn.FreepsChooseCard(armory);
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(servants));

		// "You may": the Free Peoples player is asked before the Shadow player chooses anything
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.AwaitingFellowshipPhaseActions());
		scn.FreepsChooseNo();

		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.SUPPORT, armory, servants);
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(servants));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
