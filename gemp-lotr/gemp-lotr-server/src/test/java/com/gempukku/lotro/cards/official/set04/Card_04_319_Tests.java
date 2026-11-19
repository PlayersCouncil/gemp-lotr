package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_319_Tests
{

	// The Towers path, whose sites 1 to 4 are the card's "sites 1[T] to 4[T]".  Site 4 is White Rocks 4_346, whose
	// Shadow number counts mounted companions (none here); site 5 is Deeping Wall 4_348, whose Shadow ability is used
	// only on purpose.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("severed", "4_319");
					put("severed2", "4_319");
					put("severed3", "4_319");
					put("severed4", "4_319");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3, vitality 4 (cheated in); a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3; abilities used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, strength 2; a skirmish ability used only on purpose
					put("follow", "1_109");     // One Whom Men Would Follow: [gondor] maneuver event, exert Aragorn so that an ally takes part in skirmishes

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					// Dunlending Brigand: strength 8, vitality 1, no text.  Against a Hobbit of 3, more than double and
					// less than triple.
					put("brigand1", "4_10");
					put("brigand2", "4_10");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 only; exactly triple a Hobbit of 3
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers block format, where a skirmish involving the Ring-bearer may be cancelled.
	protected VirtualTableScenario GetTowersBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("severed", "4_319");
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"ttt_block"
		);
	}

	// The Fellowship block sites: site 4 is Balin's Tomb 1_343, whose maneuver ability needs a tale.
	protected VirtualTableScenario GetFellowshipPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("severed", "4_319");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("brigand1", "4_10");    // Dunlending Brigand: strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeveredHisBondsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Severed His Bonds
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Stealth</b>.
		 * 	<b>Skirmish:</b> At sites 1[T] to 4[T], cancel a skirmish involving a Hobbit. At any other site, prevent a Hobbit from being overwhelmed unless his or her strength is tripled.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("severed");

		assertEquals("Severed His Bonds", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite4ItCancelsASkirmishInvolvingAHobbitOfAnyCultureOrSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var severed = scn.GetFreepsCard("severed");
		var severed2 = scn.GetFreepsCard("severed2");
		var severed3 = scn.GetFreepsCard("severed3");
		var severed4 = scn.GetFreepsCard("severed4");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var follow = scn.GetFreepsCard("follow");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(severed, severed2, severed3, severed4, follow);
		scn.MoveMinionsToTable(troop1, troop2, troop3, ted);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(4, scn.GetCurrentSiteNumber());
		// No Hobbit ally has a Towers home site, so he is made to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, troop1}, new PhysicalCardImpl[]{pippin, troop2},
				new PhysicalCardImpl[]{aragorn, ted}, new PhysicalCardImpl[]{bounder, troop3});

		// A [shire] Hobbit companion.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertTrue(scn.FreepsPlayAvailable(severed));
		scn.FreepsPlayCard(severed);
		assertFalse(scn.IsCharSkirmishing(merry));
		assertFalse(scn.IsCharSkirmishing(troop1));
		assertInDiscard(severed);

		// A [gondor] Hobbit companion.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, troop2));
		scn.FreepsPlayCard(severed2);
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertInDiscard(severed2);

		// The Shadow player's Hobbit, a minion.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, ted));
		scn.FreepsPlayCard(severed3);
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(ted));
		assertInDiscard(severed3);

		// A Hobbit ally.
		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, troop3));
		scn.FreepsPlayCard(severed4);
		assertFalse(scn.IsCharSkirmishing(bounder));
		assertFalse(scn.IsCharSkirmishing(troop3));
		assertInDiscard(severed4);

		// Nobody fought: no wound anywhere.
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtSite4ASkirmishInvolvingNoHobbitIsNotCancelledAndNoHobbitIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var severed = scn.GetFreepsCard("severed");
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry, smeagol);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(severed);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(smeagol, troop1);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop1));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertTrue(scn.FreepsPlayAvailable(severed));

		// Smeagol is [shire] and no Hobbit: the event is played, and nothing is cancelled. At site 4 the second
		// sentence is not reached either: no Hobbit is asked for, though Merry and Frodo are there.
		scn.FreepsPlayCard(severed);
		assertInDiscard(severed);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop1));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheAssignmentPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var severed = scn.GetFreepsCard("severed");
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(severed);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(severed));
	}

	@Test
	public void AtSite5ItKeepsAHobbitOfAnyCultureOrSideFromBeingOverwhelmedShortOfTriple() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var severed = scn.GetFreepsCard("severed");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var brigand1 = scn.GetShadowCard("brigand1");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(merry, pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(severed);
		scn.MoveMinionsToTable(brigand1, ted);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, brigand1}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(merry);

		// 8 against 3: more than double, less than triple.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, brigand1));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsPlayAvailable(severed));

		scn.FreepsPlayCard(severed);
		// Hobbits in this skirmish, in another, and in none, companions, an ally and a minion of both sides; not
		// Smeagol, Aragorn or the Brigand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, frodo, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, brigand1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertInDiscard(severed);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));
		// Nothing is cancelled at site 5.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, brigand1));

		// Merry loses and is not overwhelmed: the one wound of a lost skirmish.
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AtSite5AHobbitBeatenByTripleHisStrengthIsOverwhelmedAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var severed = scn.GetFreepsCard("severed");
		var merry = scn.GetFreepsCard("merry");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(severed);
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(merry, party);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(9, scn.GetStrength(party));
		scn.FreepsPlayCard(severed);
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

		var severed = scn.GetFreepsCard("severed");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(severed);
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, brigand1}, new PhysicalCardImpl[]{pippin, brigand2});
		scn.FreepsResolveSkirmish(merry);

		// Pippin, who waits for his own skirmish, is the Hobbit chosen during Merry's.
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		scn.FreepsPlayCard(severed);
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
	public void AtSite4OfTheFellowshipPathItProtectsAHobbitAndCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var severed = scn.GetFreepsCard("severed");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(severed);
		scn.MoveMinionsToTable(brigand1);
		scn.SkipToAssignments();
		// Site 4 of the Fellowship block is no site 4[T] (CR 4.2 "block symbol").
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(merry, brigand1);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, brigand1));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));

		scn.FreepsPlayCard(severed);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		scn.FreepsChooseCard(merry);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));

		assertTrue(scn.IsCharSkirmishingAgainst(merry, brigand1));
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InTheTowersBlockFormatItCancelsTheRingBearersSkirmishAtSite2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersBlockScenario();

		var severed = scn.GetFreepsCard("severed");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveCardsToHand(severed);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// Site 2, the first site at which a skirmish is fought.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		scn.FreepsPlayCard(severed);

		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(severed);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathItIsPlayedAndTheRingBearersSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var severed = scn.GetFreepsCard("severed");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(severed);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertEquals(2, scn.GetOverwhelmMultiplier(frodo));
		assertTrue(scn.FreepsPlayAvailable(severed));
		scn.FreepsPlayCard(severed);

		// At site 4 the second sentence does not apply either: Frodo is not protected.
		assertInDiscard(severed);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertEquals(2, scn.GetOverwhelmMultiplier(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
