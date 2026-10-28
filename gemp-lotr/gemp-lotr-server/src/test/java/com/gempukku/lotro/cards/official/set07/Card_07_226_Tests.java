package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_226_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horseman1", "7_226");
					put("horseman2", "7_226");

					put("herdsman", "7_247");   // Rohirrim Herdsman: a [rohan] Man ally; a regroup ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions (or Eowyn) can be spotted
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and no other text

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text

					// A wound to a minion in the middle of a skirmish, from the Free Peoples side.
					put("numenor", "5_37");     // Men of Numenor: Skirmish event, cost 0: wound a minion bearing a [gondor] fortification
					put("wall", "5_33");        // City Wall: a [gondor] fortification; its bearer is strength -3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnragedHorsemanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Enraged Horseman
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While you can spot another [rohan] Man and this companion is skirmishing a wounded minion, this companion is strength +2 and <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horseman1");

		assertEquals("Enraged Horseman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WithAnotherRohanManHeIsStrengthPlus2AndDamagePlus1OnlyWhileTheMinionHeIsSkirmishingIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Each Horseman is the other's "another [rohan] Man".
		var horseman1 = scn.GetFreepsCard("horseman1");
		var horseman2 = scn.GetFreepsCard("horseman2");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		scn.MoveCompanionsToTable(horseman1, horseman2);
		scn.MoveCardsToHand(numenor);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage, troop);
		// The Savage is unwounded and bears the fortification Men of Numenor needs.  The Troop has two wounds, and
		// the first Horseman one.
		scn.AttachCardsTo(savage, wall);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(horseman1, 1);
		assertInZone(Zone.ATTACHED, wall);
		assertAttachedTo(wall, savage);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(horseman1));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{horseman1, savage},
				new PhysicalCardImpl[]{horseman2, troop});
		assertEquals(5, scn.GetStrength(horseman1));
		assertEquals(5, scn.GetStrength(horseman2));
		assertEquals(0, scn.GetKeywordCount(horseman1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(horseman2, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(horseman1);
		// Against an unwounded minion nothing, though the Horseman himself is wounded.
		assertTrue(scn.IsCharSkirmishingAgainst(horseman1, savage));
		assertEquals(5, scn.GetStrength(horseman1));
		assertEquals(0, scn.GetKeywordCount(horseman1, Keyword.DAMAGE));
		// The other Horseman is assigned to a wounded minion, but that skirmish has not begun.
		assertEquals(5, scn.GetStrength(horseman2));
		assertEquals(0, scn.GetKeywordCount(horseman2, Keyword.DAMAGE));

		scn.FreepsPlayCard(numenor);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(7, scn.GetStrength(horseman1));
		assertEquals(1, scn.GetKeywordCount(horseman1, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(horseman2));
		assertEquals(0, scn.GetKeywordCount(horseman2, Keyword.DAMAGE));

		// The Savage, 5 - 3 for the wall, is beaten by more than double.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, savage);
		assertEquals(5, scn.GetStrength(horseman1));
		assertEquals(0, scn.GetKeywordCount(horseman1, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(horseman2);
		// His own skirmish against the wounded Troop: his own bonus, and nothing for the first Horseman, who is in none.
		assertTrue(scn.IsCharSkirmishingAgainst(horseman2, troop));
		assertEquals(7, scn.GetStrength(horseman2));
		assertEquals(1, scn.GetKeywordCount(horseman2, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(horseman1));
		assertEquals(0, scn.GetKeywordCount(horseman1, Keyword.DAMAGE));

		// 7 against 13: one wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(horseman2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARohanAllyIsAnotherRohanManForHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horseman = scn.GetFreepsCard("horseman1");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(horseman);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 2);
		assertEquals(2, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(horseman, troop);
		scn.FreepsResolveSkirmish(horseman);

		assertTrue(scn.IsCharSkirmishingAgainst(horseman, troop));
		assertEquals(7, scn.GetStrength(horseman));
		assertEquals(1, scn.GetKeywordCount(horseman, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherRohanManHeIsNotStrengthenedAgainstAWoundedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Merry is [rohan] and no Man (two [rohan] companions only, so he is not considered one); the Knight is a
		// Man of another culture.  The Horseman is himself a [rohan] Man, and is not "another".
		var horseman = scn.GetFreepsCard("horseman1");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(horseman, merry, knight);

		scn.StartGame();

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 2);
		assertEquals(2, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(horseman, troop);
		scn.FreepsResolveSkirmish(horseman);

		assertTrue(scn.IsCharSkirmishingAgainst(horseman, troop));
		assertEquals(5, scn.GetStrength(horseman));
		assertEquals(0, scn.GetKeywordCount(horseman, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
