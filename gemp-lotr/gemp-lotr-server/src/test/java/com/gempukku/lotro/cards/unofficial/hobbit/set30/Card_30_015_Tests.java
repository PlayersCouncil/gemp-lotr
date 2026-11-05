package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_015_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase
	// of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nori", "30_15");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion; a fellowship ability only
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, to bear Glamdring; a fellowship ability
					// and texts about [dwarven] followers and Thorin
					put("gandalf", "30_26");

					// Bifur, Inarticulate: a [dwarven] Dwarf follower, left in the support area; its Aid (exert a
					// companion) is declined, its win trigger is not reached
					put("bifur", "30_5");
					// Roäc, Son of Carc: a [dwarven] follower who is a Bird, borne by Balin; his Aid wants Thorin, who
					// is not in play, and his maneuver ability is not used
					put("roac", "32_8");
					// Gwaihir, Lord of the Eagles: a [gandalf] follower; his Aid wants a [gandalf] card in hand,
					// and the hand is empty
					put("gwaihir", "31_15");

					// Dwarven Axe: a [dwarven] possession (hand weapon) for a [dwarven] character, borne by Balin
					put("axe", "31_1");
					// Dwarven Cooking Pot: a [dwarven] possession in the support area; its texts want a token or
					// the play of Bifur, Bofur or Bombur
					put("pot", "33_5");
					// Glamdring: a [gandalf] ARTIFACT (hand weapon) on Gandalf; a win trigger not reached
					put("glamdring", "30_29");

					// Goblin Runner: [moria] Orc, so that there is a maneuver phase; cheated in, no when-played text
					put("runner", "31_30");
					// Goblin Scimitar: a [moria] possession (hand weapon) on the Runner; its texts are on its play
					put("scimitar", "31_31");
					// Hatred Rekindled: a [gundabad] condition in the support area; a Shadow ability
					put("hatred", "30_34");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void NoriStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Nori, Of the Blue Mountains
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Maneuver:</b> Exert Nori twice and discard a [dwarven] follower to discard a possession.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nori");

		assertEquals("Nori", card.getBlueprint().getTitle());
		assertEquals("Of the Blue Mountains", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ManeuverActionExertsNoriTwiceAndDiscardsADwarvenFollowerToDiscardAPossessionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nori = scn.GetFreepsCard("nori");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var axe = scn.GetFreepsCard("axe");
		var pot = scn.GetFreepsCard("pot");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(nori, balin, gandalf);
		scn.MoveCardsToSupportArea(bifur, gwaihir, pot);
		scn.AttachCardsTo(balin, axe);
		scn.AttachCardsTo(gandalf, glamdring);

		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var hatred = scn.GetShadowCard("hatred");

		scn.StartGame();
		// A follower goes back to the support area as the turn ends: Roäc is put on Balin after the start.
		scn.AttachCardsTo(balin, roac);
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, scimitar);
		scn.MoveCardsToSupportArea(hatred);
		assertAttachedTo(roac, balin);
		assertAttachedTo(axe, balin);
		assertAttachedTo(scimitar, runner);
		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.SUPPORT, bifur, gwaihir, pot, hatred);

		scn.SkipToPhase(Phase.MANEUVER);
		// Bifur's Aid is offered as the maneuver phase starts.
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(nori));
		assertTrue(scn.FreepsActionAvailable(nori));
		scn.FreepsUseCardAction(nori);

		// A [dwarven] Dwarf in the support area and a [dwarven] Bird borne by Balin; not a [gandalf] follower, and
		// not a [dwarven] card that is no follower.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, roac));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gwaihir, pot));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bifur);

		assertEquals(2, scn.GetWoundsOn(nori));
		assertInDiscard(bifur);
		assertAttachedTo(roac, balin);

		// Possessions borne and in the support area, Free Peoples and Shadow; not an artifact, not a condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, pot, scimitar));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(glamdring, hatred));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(scimitar);

		assertInDiscard(scimitar);
		assertAttachedTo(axe, balin);
		assertInZone(Zone.SUPPORT, pot, hatred, gwaihir);
		assertAttachedTo(glamdring, gandalf);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWhenNoriCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nori = scn.GetFreepsCard("nori");
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(nori, balin);
		scn.MoveCardsToSupportArea(bifur);
		scn.AttachCardsTo(balin, axe);
		// Vitality 3 with 1 wound: he can exert once, not twice.
		scn.AddWoundsToChar(nori, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(nori));
		assertInZone(Zone.SUPPORT, bifur);
		assertAttachedTo(axe, balin);
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(nori));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertInZone(Zone.SUPPORT, bifur);
		assertAttachedTo(axe, balin);
	}

	@Test
	public void ManeuverActionIsNotOfferedWithoutADwarvenFollowerToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nori = scn.GetFreepsCard("nori");
		var balin = scn.GetFreepsCard("balin");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var pot = scn.GetFreepsCard("pot");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(nori, balin);
		// A follower of another culture, and a [dwarven] card that is no follower.
		scn.MoveCardsToSupportArea(gwaihir, pot);
		scn.AttachCardsTo(balin, axe);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		assertEquals(0, scn.GetWoundsOn(nori));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(nori));
		assertEquals(0, scn.GetWoundsOn(nori));
		assertInZone(Zone.SUPPORT, gwaihir, pot);
		assertAttachedTo(axe, balin);
	}

	@Test
	public void WithNoPossessionInPlayItIsStillOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nori = scn.GetFreepsCard("nori");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bifur = scn.GetFreepsCard("bifur");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(nori, gandalf);
		scn.MoveCardsToSupportArea(bifur);
		scn.AttachCardsTo(gandalf, glamdring);

		var hatred = scn.GetShadowCard("hatred");

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.MoveCardsToSupportArea(hatred);
		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.SUPPORT, bifur, hatred);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(nori));
		assertTrue(scn.FreepsActionAvailable(nori));
		scn.FreepsUseCardAction(nori);

		// Bifur, the one [dwarven] follower, goes unasked; then nothing is a possession.
		assertEquals(2, scn.GetWoundsOn(nori));
		assertInDiscard(bifur);
		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
