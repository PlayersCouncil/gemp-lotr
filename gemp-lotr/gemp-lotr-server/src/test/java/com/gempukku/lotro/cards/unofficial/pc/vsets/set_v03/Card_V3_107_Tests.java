package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forfrodo", "103_107");
					put("aragorn", "1_89");
					put("merry", "1_302");
					put("sam", "1_311");
					put("pippin", "1_306");
					put("businessman", "13_157");
					put("ranger", "4_122");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForFrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: For Frodo
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a unique [gondor] or [shire] companion (except Frodo) to make that companion strength +1 for each burden on Frodo (limit +3 unless you cannot spot more than 4 companions).  Remove this from the game.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forfrodo");

		assertEquals("For Frodo", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ForFrodoStrengthBonusEqualsBurdensAndIsRemovedFromGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forfrodo = scn.GetFreepsCard("forfrodo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		// 2 companions (Frodo + Aragorn), well under 5 — bonus is uncapped
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(forfrodo);
		scn.AddBurdens(4); // 4 burdens on Frodo

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass(); // move to site 3

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, runner);

		// Play For Frodo — Aragorn is only valid exertion target, auto-chosen
		// Aragorn base str 8 + 4 burdens = 12
		scn.FreepsPlayCard(forfrodo);

		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetWoundsOn(aragorn)); // exerted as cost

		// Event should be removed from game, not in discard
		assertInZone(Zone.REMOVED, forfrodo);
	}

	@Test
	public void ForFrodoBonusCappedAt3With5PlusCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forfrodo = scn.GetFreepsCard("forfrodo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var runner = scn.GetShadowCard("runner");

		// 5 companions: Frodo + Aragorn + Merry + Sam + Pippin
		scn.MoveCompanionsToTable(aragorn, merry, sam, pippin);
		scn.MoveCardsToHand(forfrodo);
		scn.AddBurdens(5); // 5 burdens, but bonus should cap at +3

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass(); // move to site 3

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, runner);

		// Play For Frodo, choose Aragorn as exertion target
		// With 5+ companions, bonus is capped at +3 despite 5 burdens
		// Aragorn base str 8 + 3 (capped) = 11
		scn.FreepsPlayCard(forfrodo);
		scn.FreepsChooseCard(aragorn);

		assertEquals(11, scn.GetStrength(aragorn));
	}

	@Test
	public void ForFrodoRequiresUniqueGondorOrShireExceptFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forfrodo = scn.GetFreepsCard("forfrodo");
		var businessman = scn.GetShadowCard("businessman");
		var ranger = scn.GetShadowCard("ranger");
		var runner = scn.GetShadowCard("runner");

		// Only Frodo and non-unique Shire and Gondor companions on the table — no valid exertion target
		scn.MoveCompanionsToTable(businessman, ranger);
		scn.MoveCardsToHand(forfrodo);
		scn.AddBurdens(3);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass(); // move to site 3

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(scn.GetRingBearer(), runner);

		// Frodo is skirmishing but excluded from exertion — event should not be playable
		assertFalse(scn.FreepsPlayAvailable(forfrodo));
	}
}
