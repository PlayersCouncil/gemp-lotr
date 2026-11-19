package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("underdeeps", "13_119");

					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("troop2", "1_177");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("defend", "4_115");      // Defend It and Hope: [gondor] Maneuver event, cost 0: exert a [gondor] Man to wound a minion
					                             // once (or twice if that minion is an Uruk-hai)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King path, for its site 3, The Dimholt: underground, and a sanctuary as well.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("underdeeps", "13_119");

					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("troop2", "1_177");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("defend", "4_115");      // Defend It and Hope: [gondor] Maneuver event, cost 0: exert a [gondor] Man to wound a minion
					                             // once (or twice if that minion is an Uruk-hai)
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnderdeepsDenizenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Underdeeps Denizen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: While this minion is at an underground site, it cannot take wounds.
		*/

		var scn = GetScenario();

		var underdeeps = scn.GetFreepsCard("underdeeps");

		assertEquals("Underdeeps Denizen", underdeeps.getBlueprint().getTitle());
		assertNull(underdeeps.getBlueprint().getSubtitle());
		assertFalse(underdeeps.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, underdeeps.getBlueprint().getSide());
		assertEquals(Culture.ORC, underdeeps.getBlueprint().getCulture());
		assertEquals(CardType.MINION, underdeeps.getBlueprint().getCardType());
		assertEquals(Race.ORC, underdeeps.getBlueprint().getRace());
		assertEquals(2, underdeeps.getBlueprint().getTwilightCost());
		assertEquals(6, underdeeps.getBlueprint().getStrength());
		assertEquals(1, underdeeps.getBlueprint().getVitality());
		assertEquals(4, underdeeps.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtBalinsTombItCannotBeWoundedWhileOtherMinionsCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var underdeeps = scn.GetShadowCard("underdeeps");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(underdeeps, troop, troop2);

		// On to Balin's Tomb, site 4: underground.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(underdeeps));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Aragorn, the one [gondor] Man, exerts; the wound is offered on either Troop and not on this minion.
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, troop2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(underdeeps));
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(underdeeps));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtTheDimholtItCannotBeWoundedEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var underdeeps = scn.GetShadowCard("underdeeps");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(underdeeps, troop, troop2);

		// On to The Dimholt, site 3 of the King path: underground and a sanctuary.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(underdeeps));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, troop2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(underdeeps));
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(underdeeps));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtEttenmoorsItIsWoundedAndKilledAsAnyMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var underdeeps = scn.GetShadowCard("underdeeps");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(underdeeps, troop);

		// The move to Ettenmoors, site 2: a plains, no underground.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(underdeeps));

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(underdeeps, troop));
		scn.FreepsChooseCard(underdeeps);

		// Vitality 1: the wound kills it.
		assertInDiscard(underdeeps);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
