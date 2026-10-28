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

public class Card_12_136_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("torch1", "12_136");
					put("torch2", "12_136");

					put("whitehand", "18_131");   // White Hand Uruk: an [uruk-hai] Uruk-hai, strength 10, vitality 2; its only other text is a requirement to play, and it is cheated in
					put("tyrannical", "11_204");  // Tyrannical Uruk: an [uruk-hai] Uruk-hai, strength 14, vitality 3; Damage +1 and Toil 1 are its whole text
					put("saruman", "17_116");     // Saruman, Master of the White Hand: an [uruk-hai] Wizard minion, no Uruk-hai; each companion is resistance -1
					put("guard", "10_81");        // Cirith Ungol Guard: a [sauron] Uruk-hai; Damage +1 is its whole text
					put("savage", "1_151");       // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text

					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3; "Skirmish: Exert Gimli to make him strength +2"
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("houseguard", "5_83");    // Household Guard: strength 6, vitality 3; "Valiant" is its whole text
					put("grimir", "1_17");        // Grimir, Dwarven Elder: an ally whose home is site 3, strength 3, vitality 3; a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BerserkerTorchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Berserker Torch
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Game Text: <b>Toil 1</b>. <helper>(For each [uruk-hai] character you exert when playing this, its twilight cost is -1.)</helper><br>Bearer must be an [uruk-hai] Uruk-hai.<br>Bearer is strength +2 for each wound on each character in its skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("torch1");

		assertEquals("Berserker Torch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedOnAnUrukHaiUrukHaiByToilThatExertsAnyUrukHaiCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var torch = scn.GetShadowCard("torch1");
		var whitehand = scn.GetShadowCard("whitehand");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(torch);

		scn.StartGame();
		assertInHand(torch);
		scn.MoveMinionsToTable(whitehand, tyrannical, saruman, guard);
		scn.FreepsPass();

		// Cost 3 less 1 for one exertion.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowPlayAvailable(torch));
		scn.ShadowPlayCard(torch);

		// The bearer: an [uruk-hai] Uruk-hai of either title; not Saruman, an [uruk-hai] minion who is no Uruk-hai,
		// nor the Guard, an Uruk-hai of [sauron].
		assertTrue(scn.ShadowHasCardChoiceAvailable(whitehand, tyrannical));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, guard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(whitehand);

		// Toil: one exertion is owed, and any [uruk-hai] character may make it, Saruman included; the Guard may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(whitehand, tyrannical, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);

		assertAttachedTo(torch, whitehand);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(whitehand));
		// 2 - (3 - 1)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GrowsStrongerAsTheCompanionItsBearerIsFightingExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var torch = scn.GetShadowCard("torch1");
		var whitehand = scn.GetShadowCard("whitehand");

		scn.StartGame();
		scn.MoveMinionsToTable(whitehand);
		scn.AttachCardsTo(whitehand, torch);
		assertAttachedTo(torch, whitehand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, whitehand);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(whitehand));
		assertEquals(10, scn.GetStrength(whitehand));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(12, scn.GetStrength(whitehand));

		// A second wound on the same companion counts again.
		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(14, scn.GetStrength(whitehand));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CountsEveryWoundOnEveryCharacterInItsBearersOwnSkirmishAndOnlyWhileThatSkirmishIsFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var houseguard = scn.GetFreepsCard("houseguard");
		scn.MoveCompanionsToTable(aragorn, houseguard);

		var torch1 = scn.GetShadowCard("torch1");
		var torch2 = scn.GetShadowCard("torch2");
		var whitehand = scn.GetShadowCard("whitehand");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(whitehand, tyrannical, troop, savage);
		scn.AttachCardsTo(whitehand, torch1);
		scn.AttachCardsTo(tyrannical, torch2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(houseguard, 1);
		scn.AddWoundsToChar(whitehand, 1);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 1);
		assertAttachedTo(torch1, whitehand);
		assertAttachedTo(torch2, tyrannical);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(houseguard));
		assertEquals(1, scn.GetWoundsOn(whitehand));
		assertEquals(0, scn.GetWoundsOn(tyrannical));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.SkipToPhase(Phase.MANEUVER);
		// Wounded characters about, and no skirmish: each bearer is at its printed strength.
		assertEquals(10, scn.GetStrength(whitehand));
		assertEquals(14, scn.GetStrength(tyrannical));

		// Aragorn fights both bearers and the Goblin Patrol Troop; the Household Guard fights the Savage.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, whitehand },
				new PhysicalCardImpl[] { houseguard, savage }
		);
		scn.ShadowAssignToMinions(aragorn, tyrannical, troop);

		// The Guard's skirmish comes first.  The bearers are assigned and their own skirmish has not begun: the
		// wounds on them, on Aragorn and on the two characters now fighting count for nothing.
		scn.FreepsResolveSkirmish(houseguard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharAssigned(whitehand));
		assertFalse(scn.IsCharSkirmishing(whitehand));
		assertEquals(10, scn.GetStrength(whitehand));
		assertEquals(14, scn.GetStrength(tyrannical));

		// The Guard's 6 beats the Savage's 5, which takes a second wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(houseguard));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(whitehand));
		assertTrue(scn.IsCharSkirmishing(tyrannical));
		assertTrue(scn.IsCharSkirmishing(troop));
		assertFalse(scn.IsCharSkirmishing(savage));
		// 2 for each of 5 wounds: Aragorn's 2, the White Hand Uruk's own 1 and the Troop's 2 beside it.  Not the
		// Guard's wound, nor the 2 on the Savage, whose skirmish is over.
		assertEquals(10 + 10, scn.GetStrength(whitehand));
		// The second Torch counts the same five wounds for its own bearer, once.
		assertEquals(14 + 10, scn.GetStrength(tyrannical));
		// The Troop in the same skirmish, bearing no Torch, gets nothing.
		assertEquals(13, troop.getBlueprint().getStrength());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void AWoundOnAnAllyFightingItsBearerAtHisHomeSiteCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveAlliesToTable(grimir);

		var torch = scn.GetShadowCard("torch1");
		var tyrannical = scn.GetShadowCard("tyrannical");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(tyrannical);
		scn.AttachCardsTo(tyrannical, torch);
		scn.AddWoundsToChar(tyrannical, 1);
		scn.AddWoundsToChar(grimir, 1);
		assertAttachedTo(torch, tyrannical);
		assertEquals(1, scn.GetWoundsOn(tyrannical));
		assertEquals(1, scn.GetWoundsOn(grimir));

		// The fellowship moves to site 3, which is Grimir's home, and there he may fight.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(grimir, tyrannical);
		scn.FreepsResolveSkirmish(grimir);

		// 14 + 2 for its own wound + 2 for the ally's.
		assertEquals(18, scn.GetStrength(tyrannical));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
