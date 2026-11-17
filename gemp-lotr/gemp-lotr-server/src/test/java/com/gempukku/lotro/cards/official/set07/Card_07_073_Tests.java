package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneaking", "7_73");
					put("smeagol", "6_45");         // Sméagol, Poor Creature: [gollum] companion, vitality 4; a skirmish ability used only on purpose
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol, vitality 4; an assignment ability used only on purpose
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("fighter1", "7_286");       // Mordor Fighter: [sauron] Orc, strength 7, vitality 2; its initiative text wants a threat, and there is none
					put("fighter2", "7_286");
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: strength 8, vitality 8, fierce
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Sméagol, Bearer of Great Secrets 9_30 as the Ring-bearer.  His move trigger places an unbound companion in the
	// dead pile, and there is none on these tables.
	protected VirtualTableScenario GetRingBearerScenario(String format) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneaking", "7_73");
					put("fighter1", "7_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SmeagolRB,
				VirtualTableScenario.RulingRing,
				format
		);
	}

	@Test
	public void SneakingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Sneaking!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Stealth</b>.<br>Heal Sméagol (or cancel Sméagol's skirmish if he has more vitality than the minion or minions he is skirmishing).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sneaking");

		assertEquals("Sneaking!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoMoreVitalityThanHisMinionSmeagolIsHealedAndTheSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneaking = scn.GetFreepsCard("sneaking");
		var smeagol = scn.GetFreepsCard("smeagol");
		var fighter = scn.GetShadowCard("fighter1");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(sneaking);
		scn.AddWoundsToChar(smeagol, 2);

		scn.StartGame();
		assertInHand(sneaking);
		assertEquals(2, scn.GetWoundsOn(smeagol));
		scn.MoveMinionsToTable(fighter);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase next door: no Skirmish event in the assignment phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sneaking));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(smeagol, fighter);
		scn.FreepsResolveSkirmish(smeagol);

		// Vitality 2 left against the Fighter's 2: not more, so the heal.
		assertEquals(2, scn.GetVitality(smeagol));
		assertEquals(2, scn.GetVitality(fighter));
		assertTrue(scn.FreepsPlayAvailable(sneaking));
		scn.FreepsPlayCard(sneaking);

		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, fighter));
		assertInDiscard(sneaking);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithMoreVitalityThanHisMinionHisSkirmishIsCancelledAndHeIsNotHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneaking = scn.GetFreepsCard("sneaking");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fighter = scn.GetShadowCard("fighter1");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(sneaking);
		scn.AddWoundsToChar(smeagol, 1);

		scn.StartGame();
		assertInHand(sneaking);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		scn.MoveMinionsToTable(fighter, shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, fighter},
				new PhysicalCardImpl[]{aragorn, shelob});
		scn.FreepsResolveSkirmish(smeagol);

		// Vitality 3 left against the Fighter's 2.  Shelob's 8 is in another skirmish and does not count.
		assertEquals(3, scn.GetVitality(smeagol));
		assertEquals(2, scn.GetVitality(fighter));
		assertEquals(8, scn.GetVitality(shelob));
		scn.FreepsPlayCard(sneaking);

		// The parenthesis is mandatory where it is available (Q-0035): the skirmish is cancelled, and no heal.
		assertFalse(scn.IsCharSkirmishing(smeagol));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(fighter));
		assertInDiscard(sneaking);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, shelob));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AgainstTwoMinionsTheirVitalityIsAddedTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneaking = scn.GetFreepsCard("sneaking");
		var smeagol = scn.GetFreepsCard("smeagol");
		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(sneaking);
		scn.AddWoundsToChar(smeagol, 1);

		scn.StartGame();
		assertInHand(sneaking);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		scn.MoveMinionsToTable(fighter1, fighter2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, fighter1);
		scn.ShadowAssignToMinions(smeagol, fighter2);
		scn.FreepsResolveSkirmish(smeagol);

		// Vitality 3 against 2 and 2: more than either Fighter, not more than the two together.
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, fighter1));
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, fighter2));
		assertEquals(3, scn.GetVitality(smeagol));
		scn.FreepsPlayCard(sneaking);

		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, fighter1));
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, fighter2));
		assertInDiscard(sneaking);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheShireSmeagolInNoSkirmishIsHealedAndTheCompanionWhoFightsIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneaking = scn.GetFreepsCard("sneaking");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fighter = scn.GetShadowCard("fighter1");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(sneaking);
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		assertInHand(sneaking);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, fighter);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.IsCharSkirmishing(smeagol));
		scn.FreepsPlayCard(sneaking);

		// Smeagol has no skirmish to cancel, so he is healed; Aragorn's skirmish is not his.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, fighter));
		assertInDiscard(sneaking);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoSmeagolInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneaking = scn.GetFreepsCard("sneaking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fighter = scn.GetShadowCard("fighter1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sneaking);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		assertInHand(sneaking);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, fighter);
		scn.FreepsResolveSkirmish(aragorn);

		// An event's target is no requirement: it is played, and nobody is healed or spared.
		assertTrue(scn.FreepsPlayAvailable(sneaking));
		scn.FreepsPlayCard(sneaking);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, fighter));
		assertInDiscard(sneaking);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InMultipathTheRingBearerSmeagolsSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario(VirtualTableScenario.Multipath);

		var sneaking = scn.GetFreepsCard("sneaking");
		var smeagol = scn.GetRingBearer();
		var fighter = scn.GetShadowCard("fighter1");
		scn.MoveCardsToHand(sneaking);

		scn.StartGame();
		assertInHand(sneaking);
		scn.AddWoundsToChar(smeagol, 1);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		scn.MoveMinionsToTable(fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, fighter);
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(3, scn.GetVitality(smeagol));
		assertEquals(2, scn.GetVitality(fighter));
		assertTrue(scn.FreepsPlayAvailable(sneaking));
		scn.FreepsPlayCard(sneaking);

		// CR 4.2 "Ring-bearer": "you cannot play ... Sneaking to cancel his skirmish"; Ketura (Q-0359): it is played,
		// and the heal is done in place of the cancel.
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, fighter));
		assertInDiscard(sneaking);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InMultipathTheRingBearerSmeagolIsHealedSinceHeIsTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario(VirtualTableScenario.Multipath);

		var sneaking = scn.GetFreepsCard("sneaking");
		var smeagol = scn.GetRingBearer();
		var fighter = scn.GetShadowCard("fighter1");
		scn.MoveCardsToHand(sneaking);

		scn.StartGame();
		assertInHand(sneaking);
		scn.AddWoundsToChar(smeagol, 1);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		scn.MoveMinionsToTable(fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, fighter);
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(3, scn.GetVitality(smeagol));
		scn.FreepsPlayCard(sneaking);

		// Ketura (Q-0359): the parenthesis is for a Sméagol who is not the Ring-bearer, so he is healed.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, fighter));
		assertInDiscard(sneaking);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheFellowshipBlockFormatTooTheRingBearerSmeagolIsHealedAndHisSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario(VirtualTableScenario.Fellowship);

		var sneaking = scn.GetFreepsCard("sneaking");
		var smeagol = scn.GetRingBearer();
		var fighter = scn.GetShadowCard("fighter1");
		scn.MoveCardsToHand(sneaking);

		scn.StartGame();
		assertInHand(sneaking);
		scn.AddWoundsToChar(smeagol, 1);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		scn.MoveMinionsToTable(fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, fighter);
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(3, scn.GetVitality(smeagol));
		scn.FreepsPlayCard(sneaking);

		// The format would let the Ring-bearer's skirmish be cancelled, but Ketura (Q-0359) reads the parenthesis
		// as for a Sméagol who is not the Ring-bearer, in every format: no format has both.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, fighter));
		assertInDiscard(sneaking);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
