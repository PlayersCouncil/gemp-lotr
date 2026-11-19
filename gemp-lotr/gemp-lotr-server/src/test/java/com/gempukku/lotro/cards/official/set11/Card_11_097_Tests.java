package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dunlending", "11_97");
					put("dunlending2", "11_97");
					// Column of Easterlings: a [men] Man minion, cost 3; lurker, and its skirmish ability is never used
					put("column", "11_72");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion with no text but toil
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; his abilities are used only on purpose
					put("ted", "18_75");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion with no text
					// Goaded to War: a [men] condition that is no minion; its move trigger adds (1) and reaches no reading
					put("goaded", "12_67");

					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no other text
					put("sam", "1_311");        // Sam, Son of Hamfast: his abilities are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: his ability is used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf the Dwarf Guards' play spots
					put("guard1", "1_7");       // Dwarf Guard: "To play, spot a Dwarf", nothing more; not unique
					put("guard2", "1_7");
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally, a Free Peoples character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RagingDunlendingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Raging Dunlending
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play this minion, you may exert it to make it <b>fierce</b> until the regroup phase.<br>While you can spot 6 companions, each [men] minion is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("dunlending");

		assertEquals("Raging Dunlending", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMayExertToBeFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending = scn.GetShadowCard("dunlending");
		var dunlending2 = scn.GetShadowCard("dunlending2");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(dunlending);
		assertInHand(dunlending);

		scn.StartGame();
		// A second copy already in play, laid early, which the play must leave alone.
		scn.MoveMinionsToTable(dunlending2);
		scn.FreepsPass();
		// 4, and 2 for roaming at site 2
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(dunlending2));
		assertFalse(scn.HasKeyword(dunlending2, Keyword.FIERCE));
		scn.ShadowPlayCard(dunlending);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dunlending));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dunlending2));
		assertEquals(0, scn.GetWoundsOn(dunlending));
		assertFalse(scn.HasKeyword(dunlending, Keyword.FIERCE));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(dunlending));
		assertTrue(scn.HasKeyword(dunlending, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(dunlending2));
		assertFalse(scn.HasKeyword(dunlending2, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Knight, 6, loses to it, 10; it is still fierce in the fierce round, and is no longer once the
		// regroup phase begins.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, dunlending);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(dunlending, Keyword.FIERCE));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, dunlending);
		assertFalse(scn.HasKeyword(dunlending, Keyword.FIERCE));
	}

	@Test
	public void DeclinedItStaysUnwoundedAndNotFierceAndAnotherMenMinionPlayedAfterItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending = scn.GetShadowCard("dunlending");
		var column = scn.GetShadowCard("column");
		scn.MoveCardsToHand(dunlending, column);
		assertInHand(dunlending, column);

		scn.StartGame();
		scn.FreepsPass();
		// 4 and 3, and 2 each for roaming at site 2
		scn.SetTwilight(11);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(dunlending);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dunlending));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(dunlending));
		assertFalse(scn.HasKeyword(dunlending, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [men] Man played after it: the Dunlending, able to exert, is asked nothing.
		scn.ShadowPlayCard(column);
		assertInZone(Zone.SHADOW_CHARACTERS, column);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(dunlending));
		assertFalse(scn.HasKeyword(dunlending, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSixthCompanionPlayedMakesEachMenMinionFierceAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending = scn.GetShadowCard("dunlending");
		var legion = scn.GetShadowCard("legion");
		var ted = scn.GetShadowCard("ted");
		var troop = scn.GetShadowCard("troop");
		var goaded = scn.GetShadowCard("goaded");

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, guard1);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(guard2);

		scn.StartGame();
		// Laid early, in the fellowship phase, to be read before and after the play.
		scn.MoveMinionsToTable(dunlending, legion, ted, troop);
		scn.MoveCardsToSupportArea(goaded);

		// Five companions (Frodo among them) and an ally, who is a Free Peoples character but no companion.
		assertInZone(Zone.FREE_CHARACTERS, scn.GetRingBearer(), sam, aragorn, gimli, guard1);
		assertInZone(Zone.SUPPORT, thrarin);
		assertInZone(Zone.HAND, guard2);
		assertFalse(scn.HasKeyword(dunlending, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(legion, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goaded, Keyword.FIERCE));

		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(guard2);
		assertInZone(Zone.FREE_CHARACTERS, guard2);
		// Each [men] minion, a Man and a Hobbit, and the Dunlending itself.
		assertTrue(scn.HasKeyword(dunlending, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(legion, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(ted, Keyword.FIERCE));
		// A minion of another culture, and a [men] card that is no minion, are not.
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goaded, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
