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

public class Card_12_142_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruk", "12_142");
					put("uruk2", "12_142");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4, resistance 6.  A maneuver
					// ability, never used.
					put("aragorn", "1_89");
					// Farin, Emissary of Erebor: a [dwarven] Dwarf, vitality 3, resistance 6.  Muster at a battleground or
					// underground site only, and none is reached.
					put("farin", "11_7");
					// Gimli, Lively Combatant: a [dwarven] Dwarf, strength 6, vitality 3, resistance 7.  Draws when he wins a
					// skirmish, which he never does here.
					put("gimli", "11_8");
					// Bounder: a Hobbit ally (resistance 0, as every ally has).  A skirmish ability, never used.
					put("bounder", "1_286");
					// Hunt Them Down!: [isengard] Maneuver event, twilight 3, "Make an Uruk-hai fierce until the regroup
					// phase".
					put("hunt", "1_126");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MercilessUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Merciless Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time this minion wins a skirmish, you may spot a companion who has resistance 4 or less to place this minion on the bottom of your draw deck.
		*/

		var scn = GetScenario();

		var uruk = scn.GetFreepsCard("uruk");

		assertEquals("Merciless Uruk", uruk.getBlueprint().getTitle());
		assertNull(uruk.getBlueprint().getSubtitle());
		assertFalse(uruk.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, uruk.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, uruk.getBlueprint().getCulture());
		assertEquals(CardType.MINION, uruk.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, uruk.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(uruk, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(uruk, Keyword.DAMAGE));
		assertEquals(4, uruk.getBlueprint().getTwilightCost());
		assertEquals(11, uruk.getBlueprint().getStrength());
		assertEquals(3, uruk.getBlueprint().getVitality());
		assertEquals(5, uruk.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningWhileACompanionHasResistance4OrLessItMayGoToTheBottomOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(farin, gimli);

		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(uruk, uruk2);
		// 6, 7 and 10, each less 2 burdens: Farin, a [dwarven] Dwarf, is the only one at 4 or less.
		assertEquals(4, scn.GetResistance(farin));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(8, scn.GetResistance(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, uruk);
		// The second copy is left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// 11 against 6, short of double: Gimli loses and takes 2 wounds (damage +1), and lives.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SHADOW_CHARACTERS, uruk);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(uruk));
		// The copy that won is the one that answers.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(uruk2));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DECK, uruk);
		assertEquals(uruk, scn.GetShadowBottomOfDeck());
		// The other copy stays where it was.
		assertInZone(Zone.SHADOW_CHARACTERS, uruk2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedItStaysInPlayAndTheLoserHimselfIsTheCompanionSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		var uruk = scn.GetShadowCard("uruk");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(uruk);
		// Aragorn, a [gondor] Man, is the only one at 4 or less, and he is the one beaten.
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(8, scn.GetResistance(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(uruk));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, uruk);
		assertNotEquals(uruk, scn.GetShadowBottomOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoCompanionHasResistance4OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bounder);

		var uruk = scn.GetShadowCard("uruk");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(uruk);
		// Gimli one over the bound, Frodo at 8, and Bounder an ally (resistance 0) who is no companion.
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(bounder));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, uruk);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// It wins, but nobody can be spotted: not offered.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, uruk);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishAndLivingSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(aragorn, farin);

		var uruk = scn.GetShadowCard("uruk");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(uruk);
		// 20 against 11, short of double: the Uruk loses and takes one wound of its 3 vitality.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 12));
		assertEquals(4, scn.GetResistance(farin));
		assertEquals(0, scn.GetWoundsOn(uruk));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(20, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(uruk));
		assertInZone(Zone.SHADOW_CHARACTERS, uruk);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(aragorn, farin);

		var uruk = scn.GetShadowCard("uruk");
		var hunt = scn.GetShadowCard("hunt");
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(uruk);
		assertInHand(hunt);
		assertEquals(4, scn.GetResistance(farin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(3);
		assertFalse(scn.HasKeyword(uruk, Keyword.FIERCE));
		// Fierce, so that it skirmishes, and wins, a second time in the turn.  The one Uruk-hai is chosen unasked.
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(uruk, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(uruk));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, uruk);

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, uruk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		// Beaten again, Aragorn takes 2 more wounds and dies; the Uruk has won a second time and is offered again.
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(uruk));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(uruk, scn.GetShadowBottomOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
