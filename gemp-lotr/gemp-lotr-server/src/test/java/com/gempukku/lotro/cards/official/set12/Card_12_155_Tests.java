package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_155_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("zealot", "12_155");
					// Tyrannical Uruk: an [uruk-hai] Uruk-hai, twilight 6; Damage +1 and Toil 1, with no [uruk-hai] character to exert for it here
					put("tyrannical", "11_204");
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard and no Uruk-hai; each companion is resistance -1,
					// and a skirmish ability used only on purpose
					put("saruman", "17_116");
					put("cirith", "10_81");     // Cirith Ungol Guard: a [sauron] Uruk-hai whose whole text is Damage +1
					// Dark Alliance: an [uruk-hai] condition in the support area, no minion; a response used only on purpose
					put("alliance", "12_141");
					// Guard of the White Tree: [gondor] Man companion, strength 7, resistance 5; a skirmish ability used only on purpose
					put("guardwt", "13_67");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4, resistance 5
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4, resistance 6
					// Sméagol, Always Helps: [gollum] companion, strength 3, resistance 6; "To play, add a burden"
					put("smeagol", "7_71");
					put("ottar", "1_80");       // Ottar, Man of Laketown: [gandalf] Man ally, strength 2 (an ally's resistance is 0); a fellowship ability used only on purpose
					// Galadriel, Bearer of Wisdom: [elven] Elf companion, strength 3, resistance 3; her bonus needs an artifact or the Ring
					put("galadriel", "9_14");
					put("hunt", "1_126");       // Hunt Them Down!: Maneuver event, "Make an Uruk-hai fierce until the regroup phase"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukZealotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Uruk Zealot
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. To play, spot an [uruk-hai] minion.<br>Each companion who has resistance 0 is strength -3.<br>Each time Uruk Zealot wins a skirmish, you may draw 3 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("zealot");

		assertEquals("Uruk Zealot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnUrukhaiMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var zealot = scn.GetShadowCard("zealot");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var cirith = scn.GetShadowCard("cirith");
		var alliance = scn.GetShadowCard("alliance");
		scn.MoveCardsToHand(zealot, tyrannical);

		scn.StartGame();
		// A [sauron] Uruk-hai and an [uruk-hai] card that is no minion.
		scn.MoveMinionsToTable(cirith);
		scn.MoveCardsToSupportArea(alliance);
		assertInZone(Zone.SHADOW_CHARACTERS, cirith);
		assertInZone(Zone.SUPPORT, alliance);
		assertInHand(zealot, tyrannical);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(zealot));

		// An [uruk-hai] Uruk-hai played from hand.
		scn.ShadowPlayCard(tyrannical);
		assertInZone(Zone.SHADOW_CHARACTERS, tyrannical);
		assertTrue(scn.ShadowPlayAvailable(zealot));
		scn.ShadowPlayCard(zealot);
		assertInZone(Zone.SHADOW_CHARACTERS, zealot);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingAnUrukhaiMinionWhoIsNoUrukhai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var zealot = scn.GetShadowCard("zealot");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(zealot);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertInHand(zealot);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(zealot));
		scn.ShadowPlayCard(zealot);
		assertInZone(Zone.SHADOW_CHARACTERS, zealot);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCompanionAtResistance0IsStrengthMinus3AndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guardwt = scn.GetFreepsCard("guardwt");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ottar = scn.GetFreepsCard("ottar");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(guardwt, sam, aragorn);
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToHand(smeagol);

		var zealot = scn.GetShadowCard("zealot");

		scn.StartGame();
		scn.AddBurdens(4);
		// Cheated in during the fellowship phase, where it stands later in the turn (Q-0228).
		scn.MoveMinionsToTable(zealot);
		scn.FreepsRefreshDecision();

		// 4 burdens: the Guard and Sam at resistance 1, Aragorn at 2, Frodo at 6; Ottar, an ally, is at 0.
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetResistance(guardwt));
		assertEquals(1, scn.GetResistance(sam));
		assertEquals(7, scn.GetStrength(guardwt));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(ottar));
		assertEquals(4, scn.GetStrength(frodo));      // 3 + 1 The Ruling Ring
		assertEquals(14, scn.GetStrength(zealot));

		// Sméagol's play adds the fifth burden.
		scn.FreepsPlayCard(smeagol);
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetResistance(guardwt));
		assertEquals(0, scn.GetResistance(sam));
		// A [gondor] Man and a [shire] Hobbit at resistance 0: each 3 weaker.
		assertEquals(4, scn.GetStrength(guardwt));
		assertEquals(0, scn.GetStrength(sam));
		// Aragorn and Sméagol at 1, Frodo at 5: unchanged.  The ally at 0 is no companion, and no minion is reached.
		assertEquals(1, scn.GetResistance(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(ottar));
		assertEquals(14, scn.GetStrength(zealot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionWhoseResistanceIsTakenBelow0IsStrengthMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(galadriel, aragorn);

		var zealot = scn.GetShadowCard("zealot");

		scn.StartGame();
		scn.MoveMinionsToTable(zealot);
		scn.AddBurdens(2);
		// Galadriel at resistance 1: her printed 3.
		assertEquals(1, scn.GetResistance(galadriel));
		assertEquals(3, scn.GetStrength(galadriel));

		// Five burdens would take her to -2, which counts as 0.
		scn.AddBurdens(3);
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetResistance(galadriel));
		assertEquals(0, scn.GetStrength(galadriel));
		assertEquals(1, scn.GetResistance(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void EachSkirmishTheZealotWinsOffersThreeCardsWhichCanBeDrawnOrDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var zealot = scn.GetShadowCard("zealot");
		var hunt = scn.GetShadowCard("hunt");
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		scn.MoveMinionsToTable(zealot);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Made fierce by Hunt Them Down!, so that it fights twice.
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(zealot, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, zealot);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 14 against 8: Aragorn loses and lives, with 2 wounds (damage +1).
		assertEquals(2, scn.GetWoundsOn(aragorn));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(zealot));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertTrue(deck > 3);
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(hand + 3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());

		// The fierce round: a second win, a second offer, declined.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, zealot);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(zealot));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(hand + 3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheZealotLosingASkirmishAndLivingOffersNoDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var zealot = scn.GetShadowCard("zealot");

		scn.StartGame();
		scn.MoveMinionsToTable(zealot);
		// 18 against 14: Aragorn wins without overwhelming it (28 would).
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		assertEquals(18, scn.GetStrength(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, zealot);
		assertEquals(0, scn.GetWoundsOn(zealot));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(zealot));
		assertInZone(Zone.SHADOW_CHARACTERS, zealot);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
