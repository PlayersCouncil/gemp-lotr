package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_288_Tests
{

	// Site 1 is Bag End (FellowshipSites): the King-block Edoras Hall would make each [rohan] possession cost 1 less.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "4_288");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, home 4T; a Fellowship ability only
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, knight and nothing else
					// Merry, Master Holbytla: a [rohan] Hobbit, and a Man only while 3 [rohan] companions (or Éowyn) can be
					// spotted; beside one other [rohan] companion he is no Man
					put("merry", "103_85");
					// Rider's Mount: a [rohan] mount, "Bearer must be a [rohan] Man"; its text acts only at the start of a
					// skirmish involving its bearer
					put("mount", "4_287");
					put("mount2", "4_287");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RidersSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Rider's Spear
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [rohan] Man.<br>If bearer is mounted, bearer is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spear");

		assertEquals("Rider's Spear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAllyAndAMountedAllyBearingItIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var spear = scn.GetFreepsCard("spear");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard, knight, merry);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guma, mount);
		scn.AttachCardsTo(guard, mount2);
		scn.MoveCardsToHand(spear);

		scn.StartGame();

		assertAttachedTo(mount, guma);
		assertAttachedTo(mount2, guard);
		// Both are mounted, and neither bears the Spear yet.
		assertEquals(0, scn.GetKeywordCount(guma, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		assertTrue(scn.FreepsPlayAvailable(spear));
		scn.FreepsPlayCard(spear);

		// A [rohan] Man companion and a [rohan] Man ally; not a Man of [gondor], not the [rohan] Hobbit, not Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);

		assertAttachedTo(spear, guma);
		// The ally bearer is mounted: damage +1.  The Guard is mounted too, but bears no Spear.
		assertEquals(1, scn.GetKeywordCount(guma, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABearerIsDamagePlus1OnceAMountIsPlayedOnHimAndNotBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, spear);
		scn.AttachCardsTo(guma, mount2);
		scn.MoveCardsToHand(mount);

		scn.StartGame();

		assertAttachedTo(spear, guard);
		assertAttachedTo(mount2, guma);
		// The Guard bears the Spear, a possession that is no mount, and nothing else: he is not mounted.
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guma, Keyword.DAMAGE));

		// Guma has a mount already, so the Guard is the only bearer and the mount goes on him unasked.
		scn.FreepsPlayCard(mount);

		assertAttachedTo(mount, guard);
		assertTrue(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		// Guma is mounted and bears no Spear.
		assertEquals(0, scn.GetKeywordCount(guma, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
