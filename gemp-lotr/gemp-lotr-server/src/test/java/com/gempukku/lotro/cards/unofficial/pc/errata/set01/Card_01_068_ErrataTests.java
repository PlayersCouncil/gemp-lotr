package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_068_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrows", "51_68");

					put("legolas", "1_51");       // Legolas, Prince of Mirkwood: Elf companion, Archer, vitality 3; strength +3 against a Nazgul
					put("lorien", "1_53");        // Lorien Elf: Elf companion, no archer, vitality 2, no text in play
					put("celeborn", "1_34");      // Celeborn, Lord of Lorien: Elf ally; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose

					put("patrol", "1_177");       // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, vitality 1, so never able to exert; cheated in, its when-played twilight never comes up
					put("pinned", "1_192");       // Pinned Down: [moria] condition; only ever held in the Shadow player's hand here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWhiteArrowsofLorienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The White Arrows of Lorien
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Game Text: <b>Tale.</b> Bearer must be an Elf companion.
		 * <b>Archery:</b> If bearer is an archer, exert bearer to exert a minion.  An opponent may discard a card
		 * from hand to prevent this.
		 */

		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");

		assertEquals("The White Arrows of Lorien", arrows.getBlueprint().getTitle());
		assertNull(arrows.getBlueprint().getSubtitle());
		assertFalse(arrows.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, arrows.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, arrows.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, arrows.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(arrows, Keyword.TALE));
		assertEquals(1, arrows.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysForOneOnAnElfCompanionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(arrows);
		scn.MoveCompanionsToTable(legolas, lorien, aragorn);
		scn.MoveAlliesToTable(celeborn);
		// Aragorn is of the [elven] culture but no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ELVEN));

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(arrows));

		scn.FreepsPlayCard(arrows);
		// An archer and an Elf who is not one are both offered; the Elf ally is not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, lorien));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, aragorn, frodo));
		scn.FreepsChooseCard(lorien);

		assertAttachedTo(arrows, lorien);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryActionExertsTheArcherBearerToExertTheChosenMinionWhenTheShadowPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var lorien = scn.GetFreepsCard("lorien");
		var aragorn = scn.GetFreepsCard("aragorn");
		var patrol = scn.GetShadowCard("patrol");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas, lorien, aragorn);
		scn.AttachCardsTo(legolas, arrows);
		// The Lorien Elf is made an archer too, so that the bearer is not the only archer to be exerted.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, lorien, null, Keyword.ARCHER));
		scn.MoveCardsToShadowHand("lorien", "pinned");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, soldier, runner);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(arrows));

		scn.FreepsUseCardAction(arrows);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(lorien));

		// Minions of two cultures; the companion is no minion, and Goblin Runner cannot exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, lorien, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(soldier);

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertAttachedTo(arrows, legolas);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDiscardACardFromHandToPreventTheExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var patrol = scn.GetShadowCard("patrol");
		var soldier = scn.GetShadowCard("soldier");
		var shadowLorien = scn.GetShadowCard("lorien");
		var shadowPinned = scn.GetShadowCard("pinned");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, arrows);
		scn.MoveCardsToShadowHand("lorien", "pinned");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, soldier);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsUseCardAction(arrows);
		scn.FreepsChooseCard(soldier);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();
		// Two cards in hand: the Shadow player picks which one goes.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowLorien, shadowPinned));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(shadowPinned);

		assertInDiscard(shadowPinned);
		assertInHand(shadowLorien);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(patrol));
		// The bearer's exertion was the cost, and stays paid.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithAnEmptyHandTheShadowPlayerIsNotAskedAndTheMinionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var patrol = scn.GetShadowCard("patrol");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, arrows);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, soldier);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(soldier));

		scn.FreepsUseCardAction(arrows);
		scn.FreepsChooseCard(soldier);

		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithNoMinionAbleToExertTheBearerStillExertsAndTheShadowPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, arrows);
		scn.MoveCardsToShadowHand("lorien", "pinned");

		scn.StartGame();
		// Vitality 1: the only minion cannot be exerted.
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(arrows));

		scn.FreepsUseCardAction(arrows);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheBearerIsNotAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var lorien = scn.GetFreepsCard("lorien");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCompanionsToTable(legolas, lorien);
		// The bearer is no archer; Legolas beside him is.
		scn.AttachCardsTo(lorien, arrows);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.ARCHERY);

		assertFalse(scn.FreepsActionAvailable(arrows));

		scn.ApplyAdHocModifier(new AddKeywordModifier(null, lorien, null, Keyword.ARCHER));
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(arrows));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheArcherBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, arrows);

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(legolas, 2);
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.ARCHERY);

		assertFalse(scn.FreepsActionAvailable(arrows));

		scn.RemoveWoundsFromChar(legolas, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(arrows));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
