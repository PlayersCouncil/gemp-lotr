package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_087_Tests
{

	// The Fellowship path: site 2 is Ettenmoors 1_331, a plains (its skirmish ability is used only on purpose);
	// site 3 is Rivendell Valley 1_341, a sanctuary and no plains, home of Thrarin.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("driven", "13_87");
					put("driven2", "13_87");
					put("driven3", "13_87");
					put("driven4", "13_87");
					put("runner", "13_90");     // Easterling Runner: [men] Man minion, strength 8, no damage bonus; a Shadow ability only
					put("runner2", "13_90");
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, strength 3; abilities used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, strength 8, no text
					put("stragglers", "13_99"); // Stragglers: [men] support-area condition; its skirmish ability is used only on purpose
					put("poleaxe", "11_95");    // Poleaxe: [men] possession on a [men] minion, strength +2; its skirmish ability is used only on purpose
					put("lying", "13_95");      // Lying in Wait: [men] condition borne by a [men] minion; strength, and a required trigger on a [men] minion's win
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: resistance 6, strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: resistance 6, strength 6, vitality 3; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: resistance 5; his abilities are used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally (resistance 0), home site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrivenFromthePlainsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Driven From the Plains
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Discard this condition from play to make a [men] minion <b>damage +1</b> for each of the following that is true: it is at a plains site; it is bearing a possession; it is skirmishing a companion who has resistance 4 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("driven");

		assertEquals("Driven From the Plains", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtAPlainsBearingAPossessionAgainstResistance4AMenMinionIsDamagePlus3ForItsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var driven = scn.GetShadowCard("driven");
		var driven2 = scn.GetShadowCard("driven2");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var stragglers = scn.GetShadowCard("stragglers");
		var poleaxe = scn.GetShadowCard("poleaxe");
		scn.MoveCardsToSupportArea(driven, driven2, stragglers);

		scn.StartGame();
		// Each burden takes 1 from every companion's resistance: Aragorn's 6 is exactly 4.
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(runner, ted, brigand);
		scn.AttachCardsTo(runner, poleaxe);
		assertInZone(Zone.ATTACHED, poleaxe);
		assertInZone(Zone.SUPPORT, driven, driven2, stragglers);
		scn.SkipToAssignments();
		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{gimli, brigand});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(driven));
		scn.ShadowUseCardAction(driven);
		// A [men] minion of either race, in this skirmish or in none; not a [dunland] Man, a [men] condition or a
		// companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, stragglers, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		// The copy used is discarded; the other stays.
		assertInZone(Zone.DISCARD, driven);
		assertInZone(Zone.SUPPORT, driven2);
		// 1 at a plains + 1 the Poleaxe + 1 Aragorn at resistance 4
		assertEquals(3, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		// The bonus lasted the Runner's skirmish only.
		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));

		scn.FreepsPass();
		assertEquals(0, scn.GetKeywordCount(ted, Keyword.DAMAGE));
		scn.ShadowUseCardAction(driven2);
		scn.ShadowChooseCard(ted);
		// Ted, in no skirmish and bearing nothing, is at the plains: 1.
		assertEquals(1, scn.GetKeywordCount(ted, Keyword.DAMAGE));
		assertInZone(Zone.DISCARD, driven2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ResistanceOf5AnAllyAConditionAndASkirmishStillToComeDoNotCountAwayFromAPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(thrarin);

		var driven = scn.GetShadowCard("driven");
		var driven2 = scn.GetShadowCard("driven2");
		var driven3 = scn.GetShadowCard("driven3");
		var driven4 = scn.GetShadowCard("driven4");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var ted = scn.GetShadowCard("ted");
		var lying = scn.GetShadowCard("lying");
		scn.MoveCardsToSupportArea(driven, driven2, driven3, driven4);

		scn.StartGame();
		scn.SkipToSite(2);
		// One burden: Sam's 5 is exactly 4 and Aragorn's 6 is 5.
		scn.AddBurdens(1);
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetResistance(thrarin));
		scn.MoveMinionsToTable(runner, runner2, ted);
		scn.AttachCardsTo(runner, lying);
		assertInZone(Zone.ATTACHED, lying);
		assertInZone(Zone.SUPPORT, driven, driven2, driven3, driven4);
		scn.SkipToAssignments();
		assertEquals("Rivendell Valley", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		// Thrarin is at his home site, and may be assigned.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{sam, ted},
				new PhysicalCardImpl[]{thrarin, runner2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Aragorn has resistance 5, and Lying in Wait is a condition.
		scn.ShadowUseCardAction(driven);
		scn.ShadowChooseCard(runner);
		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));

		// Ted is assigned to Sam, of resistance 4, but their skirmish has not begun: he is skirmishing nobody.
		scn.FreepsPass();
		scn.ShadowUseCardAction(driven2);
		scn.ShadowChooseCard(ted);
		assertEquals(0, scn.GetKeywordCount(ted, Keyword.DAMAGE));

		// The Runner wins, and Lying in Wait's trigger on a [men] minion's win is ordered with the skirmish damage.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();
		// Thrarin is an ally of resistance 0, no companion.
		scn.ShadowUseCardAction(driven3);
		scn.ShadowChooseCard(runner2);
		assertEquals(0, scn.GetKeywordCount(runner2, Keyword.DAMAGE));

		// The second Runner overwhelms Thrarin: the same ordering.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();
		// Now Ted is skirmishing Sam: 1 for resistance 4.
		scn.ShadowUseCardAction(driven4);
		scn.ShadowChooseCard(ted);
		assertEquals(1, scn.GetKeywordCount(ted, Keyword.DAMAGE));
		assertInZone(Zone.DISCARD, driven, driven2, driven3, driven4);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhaseAndWithNoMenMinionIsUsedForNothingInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var driven = scn.GetShadowCard("driven");
		var driven2 = scn.GetShadowCard("driven2");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(driven, driven2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, driven, driven2);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(driven));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Brigand is the only minion, and no [men] minion: nothing can be chosen.
		assertEquals(0, scn.GetKeywordCount(brigand, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(driven));
		scn.ShadowUseCardAction(driven);

		assertInZone(Zone.DISCARD, driven);
		assertInZone(Zone.SUPPORT, driven2);
		assertEquals(0, scn.GetKeywordCount(brigand, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
