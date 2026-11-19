package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_140_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "15_140");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Guma, Plains Farmer: [rohan] Man ally, home 4T; his fellowship ability is used only on purpose
					put("guma", "4_277");
					// Merry, Master Holbytla: a [rohan] Hobbit companion; a Man only while 3 [rohan] companions (or
					// Éowyn) can be spotted, which is never so where he is laid; his texts are used only on purpose
					put("merry", "103_85");
					put("boromir", "0_65");     // Boromir, Steward's Heir: [gondor] Man companion; keywords only
					// Firefoot and Snowmane: [rohan] mounts on a [rohan] Man; each play trigger reinforces a [rohan]
					// token and is withheld with none in play, and their other texts need Éomer or Théoden
					put("firefoot", "13_126");
					put("snowmane", "13_136");
					// Heavy Chain: [rohan] armor on a Man, a possession that is no mount; its text is a bar on overwhelming
					put("chain", "4_278");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpearoftheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Spear of the Mark
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +1
		 * Resistance: +1
		 * Game Text: Bearer must be a [rohan] Man.<br>If bearer is mounted, bearer is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");

		assertEquals("Spear of the Mark", spear.getBlueprint().getTitle());
		assertNull(spear.getBlueprint().getSubtitle());
		assertFalse(spear.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, spear.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, spear.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, spear.getBlueprint().getCardType());
		assertTrue(spear.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, spear.getBlueprint().getTwilightCost());
		assertEquals(1, spear.getBlueprint().getStrength());
		assertEquals(1, spear.getBlueprint().getResistance());
	}

	@Test
	public void PlayedOnARohanManItMakesHimDefenderOnceHeIsMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var firefoot = scn.GetFreepsCard("firefoot");
		var snowmane = scn.GetFreepsCard("snowmane");
		var chain = scn.GetFreepsCard("chain");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, merry, boromir);
		scn.MoveAlliesToTable(guma);
		// The Guard bears a possession that is no mount; Guma is mounted and bears no Spear.
		scn.AttachCardsTo(guard, chain);
		scn.AttachCardsTo(guma, snowmane);
		scn.MoveCardsToHand(spear, firefoot);

		scn.StartGame();

		assertAttachedTo(chain, guard);
		assertAttachedTo(snowmane, guma);
		assertInHand(spear);
		assertEquals(0, scn.GetKeywordCount(guma, Keyword.DEFENDER));

		scn.FreepsPlayCard(spear);
		// [rohan] Men: a companion and an ally.  Not Merry (a [rohan] Hobbit, no Man beside two [rohan] companions),
		// Boromir (a [gondor] Man) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);
		assertAttachedTo(spear, guard);

		// Bearing the Spear and armor, but no mount: no defender.
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));

		// Guma has a mount already, so the Guard is Firefoot's one bearer, taken unasked.
		scn.FreepsPlayCard(firefoot);
		assertAttachedTo(firefoot, guard);

		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		// Guma is mounted but bears no Spear.
		assertEquals(0, scn.GetKeywordCount(guma, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyBearingItWhileMountedIsDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var snowmane = scn.GetFreepsCard("snowmane");
		var firefoot = scn.GetFreepsCard("firefoot");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guma, spear, snowmane);
		// The Guard is mounted and bears no Spear.
		scn.AttachCardsTo(guard, firefoot);

		scn.StartGame();

		assertAttachedTo(spear, guma);
		assertAttachedTo(snowmane, guma);
		assertAttachedTo(firefoot, guard);
		assertEquals(1, scn.GetKeywordCount(guma, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
