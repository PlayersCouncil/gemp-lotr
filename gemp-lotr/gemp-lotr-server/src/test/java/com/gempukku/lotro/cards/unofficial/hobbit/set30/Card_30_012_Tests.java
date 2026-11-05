package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_012_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kili", "30_12");
					// Legolas, Greenleaf: an [elven] archer companion, the other archer of the fellowship (no Hobbit-set
					// companion but Kili is an archer); his own Archery ability is used only on purpose
					put("legolas", "1_50");
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted); its when-played text is not used (cheated in)
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, vitality 2, fierce; a skirmish ability only
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void KiliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Kili, Brother of Fili
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Archer</b>.
		 * 	<b>Archery:</b> Exert Kili to wound a minion; Kili does not add to the fellowship archery total.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("kili");

		assertEquals("Kili", card.getBlueprint().getTitle());
		assertEquals("Brother of Fili", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ArcheryAbilityExertsKiliToWoundAMinionAndHeAddsNothingToTheArcheryTotalForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var kili = scn.GetFreepsCard("kili");
		var legolas = scn.GetFreepsCard("legolas");
		var runner = scn.GetShadowCard("runner");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(kili, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, spider);
		scn.SkipToPhase(Phase.ARCHERY);

		// Kili and Legolas each add 1.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(kili));
		assertEquals(0, scn.GetWoundsOn(spider));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.FreepsActionAvailable(kili));

		scn.FreepsUseCardAction(kili);
		assertEquals(1, scn.GetWoundsOn(kili));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, spider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(kili, legolas, bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		// The Runner, vitality 1, is wounded and so killed (an exertion would not be placed on him).
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(spider));
		assertEquals(0, scn.GetWoundsOn(legolas));
		// Kili no longer adds; Legolas still does.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// The one archery wound of the fellowship goes on the Spider, the one minion left.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetWoundsOn(spider));

		// Once the archery phase is over, Kili counts again.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileKiliIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kili = scn.GetFreepsCard("kili");
		var legolas = scn.GetFreepsCard("legolas");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(kili, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(spider);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(kili, 2);
		assertEquals(2, scn.GetWoundsOn(kili));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(kili));
		// Legolas's own Archery ability, with him able to exert, is offered beside it.
		assertTrue(scn.FreepsActionAvailable(legolas));
		assertEquals(2, scn.GetWoundsOn(kili));
	}
}
