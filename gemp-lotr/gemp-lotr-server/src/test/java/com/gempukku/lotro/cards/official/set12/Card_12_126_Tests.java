package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_126_Tests
{

	// The Fellowship path.  Its site 2 is Ettenmoors 1_331, a plains and no dwelling, whose skirmish ability is used
	// only on purpose.  Where a test wants a dwelling, a Shadows-block one is laid as site 2 by MoveCardToAdventurePath.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nww", "12_126");
					put("nww2", "12_126");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3, vitality 4 (cheated in); a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3; abilities used only on purpose

					// Dunlending Brigand: strength 8, vitality 1, no text.  Against a Hobbit of 3, more than double and
					// less than triple.
					put("brigand1", "4_10");
					put("brigand2", "4_10");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 only; exactly triple a Hobbit of 3

					// Cards to hold in hand for the discard, one of each kind; none of their text acts from the hand.
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion
					put("armor", "1_8");        // Dwarven Armor: a possession
					put("flee", "2_33");        // Flee in Terror: a [gondor] response event, answering a [gondor] companion's win over an exhausted Orc
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Streets of Bree: a Shadows-block dwelling with no other keyword; a bar on transferring followers.
					put("bree", "18_140");
					// Valley of the Silverlode: a Shadows-block dwelling and river; "When your fellowship moves to this
					// site, you may heal each Hobbit companion" (nobody is wounded, and the skip declines it).
					put("valley", "11_261");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges 9_4 as Ring-bearer: no Hobbit is in play.  His start-of-skirmish text answers only a
	// skirmish he is in, and he fights none here.
	protected VirtualTableScenario GetNoHobbitScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nww", "12_126");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("brigand1", "4_10");    // Dunlending Brigand: strength 8, vitality 1, no text
					put("armor", "1_8");        // Dwarven Armor: a possession, held in hand
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion, held in hand
					put("flee", "2_33");        // Flee in Terror: an event, held in hand as a third card to choose from
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("bree", "18_140");      // Streets of Bree: a Shadows-block dwelling; a bar on transferring followers
				}},
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoWorseforWearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: No Worse for Wear
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Prevent a Hobbit from being overwhelmed unless his or her strength is tripled. Then, if the fellowship is at a dwelling site, you may discard 2 cards from hand to return this event to your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nww");

		assertEquals("No Worse for Wear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AwayFromADwellingItKeepsAHobbitOfAnyCultureOrSideFromBeingOverwhelmedShortOfTripleAndAsksNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nww = scn.GetFreepsCard("nww");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var sam = scn.GetFreepsCard("sam");
		var armor = scn.GetFreepsCard("armor");
		var brigand1 = scn.GetShadowCard("brigand1");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(merry, pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(nww, sam, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1, ted);
		assertInHand(nww);
		assertInHand(sam);
		assertInHand(armor);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.DWELLING));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, brigand1}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(merry);

		// 8 against 3: more than double, less than triple.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, brigand1));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsPlayAvailable(nww));

		scn.FreepsPlayCard(nww);
		// Hobbits in this skirmish, in another, and in none, companions, an ally and a minion of both sides; not
		// Smeagol, Aragorn or the Brigand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, frodo, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, brigand1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertEquals(3, scn.GetOverwhelmMultiplier(merry));
		// No dwelling: nothing is asked about returning the event, which is discarded, and the hand is as it was.
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard"));
		assertInDiscard(nww);
		assertInHand(sam);
		assertInHand(armor);

		// Merry loses and is not overwhelmed: the one wound of a lost skirmish.
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AHobbitBeatenByTripleHisStrengthIsOverwhelmedAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nww = scn.GetFreepsCard("nww");
		var merry = scn.GetFreepsCard("merry");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(nww);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		assertInHand(nww);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, party);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(9, scn.GetStrength(party));
		scn.FreepsPlayCard(nww);
		// Frodo and Merry are the Hobbits to choose from.
		scn.FreepsChooseCard(merry);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));

		// 9 is three times 3: overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyTheHobbitChosenIsProtectedAndOnlyForTheSkirmishPhaseItWasMadeIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nww = scn.GetFreepsCard("nww");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.MoveCardsToHand(nww);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1, brigand2);
		assertInHand(nww);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, brigand1}, new PhysicalCardImpl[]{pippin, brigand2});
		scn.FreepsResolveSkirmish(merry);

		// Pippin, who waits for his own skirmish, is the Hobbit chosen during Merry's.
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		scn.FreepsPlayCard(nww);
		scn.FreepsChooseCard(pippin);
		assertEquals(3, scn.GetOverwhelmMultiplier(pippin));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));

		// Merry, not chosen, is overwhelmed by 8 against 3.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);

		// In the next skirmish phase the protection is gone: Pippin, 3 against 8, is overwhelmed.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, brigand2));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, pippin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InTheAssignmentPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nww = scn.GetFreepsCard("nww");
		var merry = scn.GetFreepsCard("merry");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(nww);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1);
		assertInHand(nww);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(nww));
	}

	@Test
	public void AtADwellingTwoCardsOfAnyKindDiscardedFromHandReturnTheEventToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nww = scn.GetFreepsCard("nww");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var armor = scn.GetFreepsCard("armor");
		var flee = scn.GetFreepsCard("flee");
		var troop = scn.GetFreepsCard("troop");
		var brigand1 = scn.GetShadowCard("brigand1");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(nww, sam, armor, flee, troop);

		scn.StartGame();
		scn.MoveCardToAdventurePath(bree);
		scn.MoveMinionsToTable(brigand1);
		assertInHand(nww);
		assertInHand(sam);
		assertInHand(armor);
		assertInHand(flee);
		assertInHand(troop);
		assertEquals(5, scn.GetFreepsHandCount());
		scn.SkipToAssignments();
		assertSame(bree, scn.GetCurrentSite());
		scn.FreepsAssignToMinions(merry, brigand1);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		scn.FreepsPlayCard(nww);
		scn.FreepsChooseCard(merry);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 2 cards"));
		scn.FreepsChooseYes();

		// Exactly two, of any kind: a companion, a possession, an event and a Shadow card.  The event itself is
		// being played and is no card in hand (CR 4.2 "playing a card").
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, armor, flee, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nww));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sam, troop);

		assertInDiscard(sam);
		assertInDiscard(troop);
		assertInHand(armor);
		assertInHand(flee);
		assertInHand(nww);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtADwellingThatIsARiverTheReturnIsOfferedAndDecliningItLeavesTheHandAndDiscardsTheEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nww = scn.GetFreepsCard("nww");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var armor = scn.GetFreepsCard("armor");
		var brigand1 = scn.GetShadowCard("brigand1");
		var valley = scn.GetShadowSite("valley");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(nww, sam, armor);

		scn.StartGame();
		scn.MoveCardToAdventurePath(valley);
		scn.MoveMinionsToTable(brigand1);
		assertInHand(nww);
		assertInHand(sam);
		assertInHand(armor);
		scn.SkipToAssignments();
		assertSame(valley, scn.GetCurrentSite());
		scn.FreepsAssignToMinions(merry, brigand1);
		scn.FreepsResolveSkirmish(merry);

		scn.FreepsPlayCard(nww);
		scn.FreepsChooseCard(merry);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 2 cards"));
		scn.FreepsChooseNo();

		assertInDiscard(nww);
		assertInHand(sam);
		assertInHand(armor);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtADwellingWithOneCardInHandTheReturnIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nww = scn.GetFreepsCard("nww");
		var merry = scn.GetFreepsCard("merry");
		var armor = scn.GetFreepsCard("armor");
		var brigand1 = scn.GetShadowCard("brigand1");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(nww, armor);

		scn.StartGame();
		scn.MoveCardToAdventurePath(bree);
		scn.MoveMinionsToTable(brigand1);
		assertInHand(nww);
		assertInHand(armor);
		scn.SkipToAssignments();
		assertSame(bree, scn.GetCurrentSite());
		scn.FreepsAssignToMinions(merry, brigand1);
		scn.FreepsResolveSkirmish(merry);

		scn.FreepsPlayCard(nww);
		scn.FreepsChooseCard(merry);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));

		// One card in hand cannot pay for the return.
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard 2 cards"));
		assertInDiscard(nww);
		assertInHand(armor);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoHobbitInPlayItIsPlayedForNothingAndAtADwellingTheReturnIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoHobbitScenario();

		var nww = scn.GetFreepsCard("nww");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetFreepsCard("troop");
		var flee = scn.GetFreepsCard("flee");
		var brigand1 = scn.GetShadowCard("brigand1");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(nww, armor, troop, flee);

		scn.StartGame();
		scn.MoveCardToAdventurePath(bree);
		scn.MoveMinionsToTable(brigand1);
		assertInHand(nww);
		assertInHand(armor);
		assertInHand(troop);
		assertInHand(flee);
		scn.SkipToAssignments();
		assertSame(bree, scn.GetCurrentSite());
		scn.FreepsAssignToMinions(aragorn, brigand1);
		scn.FreepsResolveSkirmish(aragorn);

		// An event's target is no requirement: with no Hobbit to protect it is still played.
		assertTrue(scn.FreepsPlayAvailable(nww));
		scn.FreepsPlayCard(nww);

		// The "Then" sentence stands by itself (Q-0314): the return is still asked.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 2 cards"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, troop, flee));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nww));
		scn.FreepsChooseCards(armor, troop);
		assertInDiscard(armor);
		assertInDiscard(troop);
		assertInHand(flee);
		assertInHand(nww);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
