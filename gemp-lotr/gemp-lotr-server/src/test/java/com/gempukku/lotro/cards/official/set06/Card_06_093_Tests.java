package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope", "6_93");
					// Household Guard: [rohan] Man companion, strength 6, vitality 3, not unique; Valiant is its whole text
					put("guard", "5_83");
					put("guard2", "5_83");
					// Eowyn, Daughter of Eomund: [rohan] Man companion, strength 6, vitality 3; her maneuver ability is
					// used only on purpose
					put("eowyn", "5_122");
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY, vitality 2; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man of [gondor], vitality 4
					// Merry, Master Holbytla: a [rohan] Hobbit, strength 3, who is a Man as well only while Eowyn
					// (or 3 [rohan] companions) can be spotted; his own abilities are used only on purpose
					put("merry", "103_85");
					// Rider's Mount: [rohan] mount on a [rohan] Man; at the start of each skirmish involving bearer,
					// each minion skirmishing bearer must exert. It changes no strength
					put("mount", "4_287");
					put("mount2", "4_287");
					put("mount3", "4_287");
					// Horse of Rohan: the same mount, on a Man, Elf or Wizard: the mount a Man of Gondor bears
					put("horse", "4_283");
					put("spear", "4_288");      // Rider's Spear: a [rohan] possession on a [rohan] Man that is no mount

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, vitality 2; +3 and fierce only while 2 burdens can be spotted
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul, fierce, vitality 3; strength +1 for each companion
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EvertheHopeofMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Ever the Hope of Men
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert 2 mounted [rohan] Men to discard up to 2 wounded minions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hope");

		assertEquals("Ever the Hope of Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTwoMountedRohanMenToDiscardUpTo2WoundedMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var mount3 = scn.GetFreepsCard("mount3");
		var horse = scn.GetFreepsCard("horse");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(guard, guard2, eowyn, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(eowyn, mount2);
		scn.AttachCardsTo(guma, mount3);
		scn.AttachCardsTo(aragorn, horse);
		scn.AttachCardsTo(guard2, spear);
		scn.MoveCardsToHand(hope);

		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		var rider = scn.GetShadowCard("rider");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(mount2, eowyn);
		assertAttachedTo(mount3, guma);
		assertAttachedTo(horse, aragorn);
		assertAttachedTo(spear, guard2);
		scn.AddWoundsToChar(guard2, 1);
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertInHand(hope);
		scn.FreepsPass();
		// Laid in the Shadow phase and left unassigned, they live on into the regroup phase.
		scn.MoveMinionsToTable(savage, axeman, rider, troop);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(axeman, 1);
		scn.AddWoundsToChar(rider, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(hope));
		scn.FreepsPlayCard(hope);

		// Mounted [rohan] Men: two companions and an ally. Not a [rohan] Man bearing a possession that is no mount,
		// nor a mounted Man of [gondor].
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, eowyn, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard2, aragorn));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(eowyn, guma);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard2));

		// Wounded minions of three cultures and races; not an unwounded minion, nor a wounded companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, axeman, rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, guard2, eowyn));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(savage, rider);

		assertInDiscard(savage);
		assertInDiscard(rider);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.FREE_CHARACTERS, guard2);
		assertInDiscard(hope);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoWoundedMinionItIsPlayedAndOnlyTheMenExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var guard = scn.GetFreepsCard("guard");
		var eowyn = scn.GetFreepsCard("eowyn");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard, eowyn);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(eowyn, mount2);
		scn.MoveCardsToHand(hope);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(mount2, eowyn);
		assertInHand(hope);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsPlayAvailable(hope));

		// The two mounted Men exert unasked; nothing is there to discard.
		scn.FreepsPlayCard(hope);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInDiscard(hope);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneMountedRohanManAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var horse = scn.GetFreepsCard("horse");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(guard, guard2, eowyn, aragorn);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(eowyn, mount2);
		scn.AttachCardsTo(aragorn, horse);
		scn.AttachCardsTo(guard2, spear);
		scn.MoveCardsToHand(hope);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(mount2, eowyn);
		assertAttachedTo(horse, aragorn);
		assertAttachedTo(spear, guard2);
		scn.AddWoundsToChar(eowyn, 2);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertInHand(hope);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.REGROUP);

		// Eowyn, vitality 3 with 2 wounds, cannot exert; the Guard with the Spear and Aragorn on the Horse could, but
		// the one is not mounted and the other is no [rohan] Man. A wounded minion is there to discard.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsPlayAvailable(hope));
		assertInHand(hope);
	}

	@Test
	public void AMountedRohanHobbitIsNoRohanManToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var eowyn = scn.GetFreepsCard("eowyn");
		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(eowyn, merry);
		scn.MoveAlliesToTable(guma);
		// Beside Eowyn he is a [rohan] Man and may bear the mount.
		scn.AttachCardsTo(merry, mount);
		scn.AttachCardsTo(guma, mount2);
		scn.MoveCardsToHand(hope);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(mount, merry);
		assertAttachedTo(mount2, guma);
		assertInHand(hope);
		scn.MoveMinionsToTable(troop);
		// A wound for the event to find, once the Troop has won.
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, troop);
		scn.FreepsResolveSkirmish(eowyn);

		// Eowyn, 6 against 13, is overwhelmed, and with her gone Merry is a Hobbit and no Man.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, eowyn);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertAttachedTo(mount, merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(troop));
		// Guma, a mounted [rohan] Man, could exert; mounted Merry is no Man.
		assertFalse(scn.FreepsPlayAvailable(hope));
		assertInHand(hope);
	}
}
