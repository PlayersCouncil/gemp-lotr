package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_268_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "4_268");
					put("eomer", "4_266");      // Éomer, Sister-son of Théoden: [rohan] Man companion, no damage bonus; a start-of-turn heal of a [rohan] ally
					put("theoden", "4_292");    // Théoden, Son of Thengel: [rohan] Man companion who is no Éomer; a Regroup ability only
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, home 4T; a Fellowship ability only
					// Merry, Master Holbytla: a [rohan] Hobbit, and a Man only while 3 [rohan] companions (or Éowyn) can be
					// spotted; beside one other [rohan] companion he is no Man
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a Maneuver ability only
					// Éomer's Bow: a [rohan] ranged weapon, no mount; strength +1 to a mounted bearer, an Archery ability
					put("bow", "18_95");
					// Firefoot, Mearas of The Mark: a [rohan] mount; its play trigger reinforces a [rohan] token (none in
					// play, so not offered) and its maneuver trigger is not reached
					put("mount", "13_126");
					// Snowmane, Noble Mearas: a [rohan] mount; its play trigger is not reached, its Skirmish ability needs Théoden
					put("mount2", "13_136");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomersSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Éomer's Spear
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [rohan] Man.<br>If bearer is Éomer and he is mounted, Éomer is <b>damage +2</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spear");

		assertEquals("Éomer's Spear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void EomersSpearIsPlayedOnlyOnARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eomer, merry, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(spear);

		scn.StartGame();

		assertInHand(spear);
		assertTrue(scn.FreepsPlayAvailable(spear));
		scn.FreepsPlayCard(spear);

		// A [rohan] companion and a [rohan] ally, both Men; not Merry, a [rohan] Hobbit (two [rohan] companions:
		// no Man), nor Aragorn, a Man of another culture, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);

		assertAttachedTo(spear, guma);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EomerBearingTheSpearIsDamagePlus2OnceAMountIsPlayedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var eomer = scn.GetFreepsCard("eomer");
		var bow = scn.GetFreepsCard("bow");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, spear, bow);
		scn.MoveCardsToHand(mount);

		scn.StartGame();

		// He bears a possession, the Bow, but no mount: he is not mounted.
		assertAttachedTo(spear, eomer);
		assertAttachedTo(bow, eomer);
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DAMAGE));

		scn.FreepsPlayCard(mount);
		assertAttachedTo(mount, eomer);
		assertEquals(2, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByAnotherMountedRohanManTheSpearMakesNobodyDamagePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var eomer = scn.GetFreepsCard("eomer");
		var theoden = scn.GetFreepsCard("theoden");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(eomer, theoden);
		scn.AttachCardsTo(theoden, spear, mount);
		scn.AttachCardsTo(eomer, mount2);

		scn.StartGame();

		// Théoden is mounted and bears the Spear, but is no Éomer; Éomer is mounted but does not bear it.
		assertAttachedTo(spear, theoden);
		assertAttachedTo(mount, theoden);
		assertAttachedTo(mount2, eomer);
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
