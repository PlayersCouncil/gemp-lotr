package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fear", "2_56");
					put("balrog", "12_79");      // The Balrog, The Terror of Khazad-dum: vitality 5; his text acts only underground, which sites 2 and 3 are not
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] minion with no text, no Balrog
					put("soldier", "4_220");     // Desert Soldier: a [raider] Man; his trigger needs a skirmish
					put("southronbow", "4_247"); // Southron Bow: a [raider] ranged weapon, the Shadow player's own
					put("legolas", "1_50");      // Legolas, Greenleaf: an Elf archer; his archery ability is used only on purpose
					put("elvenbow", "1_41");     // Elven Bow: an [elven] ranged weapon for an Elf
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: his maneuver ability is used only on purpose
					put("aragornsbow", "1_90");  // Aragorn's Bow: a [gondor] ranged weapon
					put("sword", "1_112");       // Ranger's Sword: a hand weapon for Aragorn, no ranged weapon
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FillWithFearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Fill With Fear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * While you can spot The Balrog, skip the archery phase.
		 * <b>Maneuver:</b> Exert The Balrog to discard a ranged weapon.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fear");

		assertEquals("Fill With Fear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OnceTheBalrogIsPlayedTheArcheryPhaseIsSkippedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var balrog = scn.GetShadowCard("balrog");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(fear);
		scn.MoveCardsToHand(balrog);

		scn.StartGame();
		scn.SetTwilight(20);

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(balrog));
		scn.ShadowPlayCard(balrog);
		assertInPlay(balrog);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// From the maneuver phase straight to the assignment phase: Legolas never fires, and neither player was
		// asked whether to skip (CR 4.2 "spot": it either works or it doesn't).
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(balrog));
	}

	@Test
	public void WithAMoriaMinionWhoIsNotTheBalrogTheArcheryPhaseIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPass();
		scn.ShadowPass();
		// Legolas's archery wound on the only minion.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsTheBalrogToDiscardARangedWeaponOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var balrog = scn.GetShadowCard("balrog");
		var soldier = scn.GetShadowCard("soldier");
		var southronbow = scn.GetShadowCard("southronbow");
		var legolas = scn.GetFreepsCard("legolas");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornsbow = scn.GetFreepsCard("aragornsbow");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(legolas, elvenbow);
		scn.AttachCardsTo(aragorn, aragornsbow, sword);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, soldier);
		scn.AttachCardsTo(soldier, southronbow);
		assertAttachedTo(elvenbow, legolas);
		assertAttachedTo(aragornsbow, aragorn);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(southronbow, soldier);

		// The archery phase is skipped while The Balrog is in play, so the archers make no archery wounds.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertTrue(scn.ShadowActionAvailable(fear));

		scn.ShadowUseCardAction(fear);
		assertEquals(1, scn.GetWoundsOn(balrog));
		// Ranged weapons of three cultures, the Shadow player's own among them; not a hand weapon.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elvenbow, aragornsbow, southronbow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragornsbow);

		assertInDiscard(aragornsbow);
		assertAttachedTo(elvenbow, legolas);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(southronbow, soldier);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoRangedWeaponInPlayTheAbilityExertsTheBalrogForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var balrog = scn.GetShadowCard("balrog");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		assertAttachedTo(sword, aragorn);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertTrue(scn.ShadowActionAvailable(fear));

		scn.ShadowUseCardAction(fear);
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileTheBalrogIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, elvenbow);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, troop);
		// Vitality 5 with 4 wounds: exhausted.  The Troop beside him could exert, but is no Balrog.
		scn.AddWoundsToChar(balrog, 4);
		assertEquals(4, scn.GetWoundsOn(balrog));
		assertAttachedTo(elvenbow, legolas);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fear));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertAttachedTo(elvenbow, legolas);
	}
}
