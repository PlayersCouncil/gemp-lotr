package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_014_Tests
{

	// The King path, whose sites 1 to 5 are the card's "sites 1[K] to 5[K]".  Site 2 is King's Tent 7_335 (a Shadow
	// ability used only on purpose); site 5 is Pelennor Flat 7_345, where the Free Peoples player must discard his
	// hand or add 2 burdens at the start of the maneuver phase (he adds the burdens); site 6 is Minas Tirith
	// Seventh Circle 7_350, whose text answers only a regroup move.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fool", "8_14");
					put("fool2", "8_14");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; a standing strength bonus only
					put("ent", "6_27");         // Ent Avenger: a [gandalf] companion who is no Gandalf, vitality 4
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3, vitality 4 (cheated in); a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3; abilities used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, strength 2; a skirmish ability used only on purpose

					// Dunlending Brigand: strength 8, vitality 1, no text.  Against a Hobbit of 3, more than double and
					// less than triple.
					put("brigand1", "4_10");
					put("brigand2", "4_10");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 only; exactly triple a Hobbit of 3
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship block sites: site 4 is Balin's Tomb 1_343, whose maneuver ability needs a tale.
	protected VirtualTableScenario GetFellowshipPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fool", "8_14");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; a standing strength bonus only
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("brigand1", "4_10");    // Dunlending Brigand: strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AFoolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: A Fool
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: At sites 1[K] to 5[K], heal Gandalf. At any other site, prevent a Hobbit from being overwhelmed unless his or her strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fool");

		assertEquals("A Fool", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtKingSite5ItHealsGandalfAndNoOtherCompanionAndProtectsNoHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fool = scn.GetFreepsCard("fool");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var merry = scn.GetFreepsCard("merry");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(gandalf, ent, merry);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(fool);
		scn.MoveMinionsToTable(brigand1);
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(ent, 1);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(ent));
		assertInHand(fool);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsChooseOption("burden");
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(merry, brigand1);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertTrue(scn.FreepsPlayAvailable(fool));
		scn.FreepsPlayCard(fool);

		// Gandalf is healed; Ent Avenger, a [gandalf] companion of another title, is not.
		assertInDiscard(fool);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(ent));
		// At site 5 the second sentence does not apply: Merry is not protected, and 8 against 3 overwhelms him.
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtKingSite2ItIsPlayedForNothingWithGandalfUnwoundedAndHealsHimOnceHeIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fool = scn.GetFreepsCard("fool");
		var fool2 = scn.GetFreepsCard("fool2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var aragorn = scn.GetFreepsCard("aragorn");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(gandalf, ent, aragorn);
		scn.MoveCardsToHand(fool, fool2);
		scn.AddWoundsToChar(ent, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(ent));
		assertInHand(fool);
		assertInHand(fool2);
		scn.MoveMinionsToTable(brigand1);
		scn.SkipToAssignments();
		// Site 2, the first site at which a skirmish is fought (the low end of "1[K] to 5[K]").
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, brigand1);
		scn.FreepsResolveSkirmish(aragorn);

		// With Gandalf unwounded the event is played for nothing.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(fool));
		scn.FreepsPlayCard(fool);
		assertInDiscard(fool);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(ent));

		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.ShadowPass();
		scn.FreepsPlayCard(fool2);
		assertInDiscard(fool2);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(ent));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheAssignmentPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fool = scn.GetFreepsCard("fool");
		var gandalf = scn.GetFreepsCard("gandalf");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fool);
		scn.AddWoundsToChar(gandalf, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInHand(fool);
		scn.MoveMinionsToTable(brigand1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fool));
	}

	@Test
	public void AtKingSite6ItKeepsAHobbitOfAnyCultureOrSideFromBeingOverwhelmedShortOfTripleAndHealsNoGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fool = scn.GetFreepsCard("fool");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var brigand1 = scn.GetShadowCard("brigand1");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(gandalf, merry, pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(fool);
		scn.MoveMinionsToTable(brigand1, ted);
		scn.AddWoundsToChar(gandalf, 2);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInHand(fool);
		scn.SkipToAssignments();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, brigand1}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(merry);

		// 8 against 3: more than double, less than triple.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, brigand1));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsPlayAvailable(fool));

		scn.FreepsPlayCard(fool);
		// Hobbits in this skirmish, in another, and in none, companions, an ally and a minion of both sides; not
		// Smeagol, Aragorn, Gandalf or the Brigand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, frodo, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, gandalf, brigand1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertInDiscard(fool);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));
		// At site 6 the first sentence does not apply: Gandalf is not healed.
		assertEquals(2, scn.GetWoundsOn(gandalf));

		// Merry loses and is not overwhelmed: the one wound of a lost skirmish.
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AtKingSite6AHobbitBeatenByTripleHisStrengthIsOverwhelmedAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fool = scn.GetFreepsCard("fool");
		var merry = scn.GetFreepsCard("merry");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(fool);
		scn.MoveMinionsToTable(party);
		assertInHand(fool);
		scn.SkipToAssignments();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(merry, party);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(9, scn.GetStrength(party));
		scn.FreepsPlayCard(fool);
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

		var fool = scn.GetFreepsCard("fool");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(fool);
		scn.MoveMinionsToTable(brigand1, brigand2);
		assertInHand(fool);
		scn.SkipToAssignments();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, brigand1}, new PhysicalCardImpl[]{pippin, brigand2});
		scn.FreepsResolveSkirmish(merry);

		// Pippin, who waits for his own skirmish, is the Hobbit chosen during Merry's.
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		scn.FreepsPlayCard(fool);
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
	public void AtSite4OfTheFellowshipPathItProtectsAHobbitAndHealsNoGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var fool = scn.GetFreepsCard("fool");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(gandalf, merry);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(fool);
		scn.MoveMinionsToTable(brigand1);
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInHand(fool);
		scn.SkipToAssignments();
		// Site 4 of the Fellowship block is no site 4[K] (CR 4.2 "block symbol").
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(merry, brigand1);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, brigand1));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));

		scn.FreepsPlayCard(fool);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		scn.FreepsChooseCard(merry);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));
		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
