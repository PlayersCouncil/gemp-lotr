package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_156_Tests
{

	// The Ring-bearer is Frodo, Son of Drogo, who is kept out of the skirmishes.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halberd", "7_156");

					// Easterling Infantry: [raider] Easterling Man, vitality 2; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
					// Southron Explorer: [raider] Southron Man, vitality 3; a regroup ability only
					put("explorer", "4_250");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul); his text needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RaiderHalberdStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Raider Halberd
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [raider] Man.<br><b>Skirmish:</b> Exert bearer to add (2).
		*/

		var scn = GetScenario();

		var halberd = scn.GetFreepsCard("halberd");

		assertEquals("Raider Halberd", halberd.getBlueprint().getTitle());
		assertNull(halberd.getBlueprint().getSubtitle());
		assertFalse(halberd.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, halberd.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, halberd.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, halberd.getBlueprint().getCardType());
		assertTrue(halberd.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, halberd.getBlueprint().getTwilightCost());
		assertEquals(2, halberd.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnARaiderManOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halberd = scn.GetShadowCard("halberd");
		var infantry = scn.GetShadowCard("infantry");
		var explorer = scn.GetShadowCard("explorer");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(halberd);
		assertInHand(halberd);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, explorer, khamul, brigand);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(halberd));

		scn.ShadowPlayCard(halberd);
		// An Easterling and a Southron, [raider] Men both; not the [raider] Nazgul, the [dunland] Man or Frodo.
		assertTrue(scn.ShadowHasCardChoiceAvailable(infantry, explorer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(infantry);

		assertAttachedTo(halberd, infantry);
		assertNotAttachedTo(halberd, explorer);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsTheSkirmishingBearerToAddTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halberd = scn.GetShadowCard("halberd");
		var infantry = scn.GetShadowCard("infantry");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, explorer);
		scn.AttachCardsTo(infantry, halberd);
		assertAttachedTo(halberd, infantry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, infantry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(4, scn.GetTwilight()); // 2 for Ettenmoors + 2 companions
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.ShadowActionAvailable(halberd));

		scn.ShadowUseCardAction(halberd);

		// The bearer exerts, unasked; the other [raider] Man, who could exert, does not.
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(6, scn.GetTwilight()); // 4 + 2
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsUsedByABearerInNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halberd = scn.GetShadowCard("halberd");
		var infantry = scn.GetShadowCard("infantry");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, explorer);
		scn.AttachCardsTo(explorer, halberd);
		assertAttachedTo(halberd, explorer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, infantry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The text names no skirmish: the bearer, assigned to none, may still use it in the Infantry's.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(4, scn.GetTwilight()); // 2 for Ettenmoors + 2 companions
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertTrue(scn.ShadowActionAvailable(halberd));

		scn.ShadowUseCardAction(halberd);

		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertEquals(6, scn.GetTwilight()); // 4 + 2
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halberd = scn.GetShadowCard("halberd");
		var infantry = scn.GetShadowCard("infantry");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, explorer);
		scn.AttachCardsTo(infantry, halberd);
		// Vitality 2 with 1 wound: the bearer is exhausted; the [raider] Man beside him could exert.
		scn.AddWoundsToChar(infantry, 1);
		assertAttachedTo(halberd, infantry);
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertTrue(scn.IsExhausted(infantry));
		assertFalse(scn.IsExhausted(explorer));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, infantry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(halberd));
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(0, scn.GetWoundsOn(explorer));
	}
}
