package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blood", "6_91");
					// Eowyn, Daughter of Eomund: [rohan] Man, strength 6, vitality 3; her maneuver ability is used
					// only on purpose
					put("eowyn", "5_122");
					// Household Guard: [rohan] Man, strength 6, vitality 3, not unique; Valiant is its whole text
					put("guard", "5_83");
					put("guard2", "5_83");
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man who is an ally (home 4T)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man of [gondor]
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

					// The Witch-king, Black Captain: strength 14, vitality 4, enduring (strength +2 for each wound)
					put("twk", "8_84");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
					// Black Rider: fierce; strength 8, +1 for each companion; vitality 3
					put("rider", "12_161");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BloodHasBeenSpilledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Blood Has Been Spilled
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Plays to your support area.<br><b>Skirmish:</b> Discard a mount borne by a [rohan] Man to prevent that Man from being overwhelmed unless his or her strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blood");

		assertEquals("Blood Has Been Spilled", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysToTheSupportAreaForOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood");
		scn.MoveCardsToHand(blood);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(blood));
		scn.FreepsPlayCard(blood);

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, blood);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsAMountBorneByARohanManSoThatUnderTripleHeIsNotOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var mount3 = scn.GetFreepsCard("mount3");
		var horse = scn.GetFreepsCard("horse");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(eowyn, guard, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(eowyn, mount);
		scn.AttachCardsTo(guard, mount2, spear);
		scn.AttachCardsTo(guma, mount3);
		scn.AttachCardsTo(aragorn, horse);
		scn.MoveCardsToSupportArea(blood);

		var twk = scn.GetShadowCard("twk");

		scn.StartGame();
		scn.MoveMinionsToTable(twk);

		// A skirmish action: not offered in the assignment phase before it.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(blood));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, twk);
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(6, scn.GetStrength(eowyn));
		// 14 + 2: enduring, and exerted by Eowyn's mount as the skirmish began. Double her 6, and under triple.
		assertEquals(16, scn.GetStrength(twk));
		assertAttachedTo(mount, eowyn);
		assertTrue(scn.FreepsActionAvailable(blood));
		scn.FreepsUseCardAction(blood);

		// A mount on a [rohan] Man: the Man in the skirmish, the Man in none, the ally. Not the mount a Man of
		// Gondor bears, and not a [rohan] Man's spear.
		assertTrue(scn.FreepsHasCardChoiceAvailable(mount, mount2, mount3));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(horse, spear));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(mount);

		assertInZone(Zone.DISCARD, mount);
		assertAttachedTo(mount2, guard);
		assertAttachedTo(mount3, guma);
		assertAttachedTo(horse, aragorn);
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(16, scn.GetStrength(twk));

		scn.ShadowPass();
		scn.FreepsPass();

		// She loses and takes the one wound of a lost skirmish.
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertInZone(Zone.FREE_CHARACTERS, eowyn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ProtectsOneShortOfTripleAndNotAtTriple() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard, guard2);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(guard2, mount2);
		scn.MoveCardsToSupportArea(blood);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troop2);
		// Each Guard is 6: one Troop is brought to 17 and the other to 18.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 4));
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop2, null, 5));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{guard2, troop2});

		scn.FreepsResolveSkirmish(guard);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(17, scn.GetStrength(troop));
		scn.FreepsUseCardAction(blood);
		scn.FreepsChooseCard(mount);
		assertInZone(Zone.DISCARD, mount);
		scn.ShadowPass();
		scn.FreepsPass();

		// 17 is not three times 6: he is beaten and wounded.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.FREE_CHARACTERS, guard);

		scn.FreepsResolveSkirmish(guard2);
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(18, scn.GetStrength(troop2));
		// One mount is left on a [rohan] Man, so it is discarded without a choice.
		scn.FreepsUseCardAction(blood);
		assertInZone(Zone.DISCARD, mount2);
		scn.ShadowPass();
		scn.FreepsPass();

		// 18 is three times 6: overwhelmed all the same.
		assertInZone(Zone.DEAD, guard2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoRohanManBearsAMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var horse = scn.GetFreepsCard("horse");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(guard, aragorn);
		// The [rohan] Man bears a possession that is no mount, and the mount is borne by a Man of Gondor.
		scn.AttachCardsTo(guard, spear);
		scn.AttachCardsTo(aragorn, horse);
		scn.MoveCardsToSupportArea(blood);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.IsCharSkirmishing(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(blood));
	}

	@Test
	public void UsedOnAnotherMansMountTheManInTheSkirmishIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard, guard2);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(guard2, mount2);
		scn.MoveCardsToSupportArea(blood);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		// 13 against 6: more than double.
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsUseCardAction(blood);
		scn.FreepsChooseCard(mount2);
		assertInZone(Zone.DISCARD, mount2);
		assertAttachedTo(mount, guard);

		scn.ShadowPass();
		scn.FreepsPass();

		// "That Man" is the one whose mount was discarded, and he is in no skirmish.
		assertInZone(Zone.DEAD, guard);
		assertInZone(Zone.FREE_CHARACTERS, guard2);
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMountOnMerryPaysOnlyWhileHeIsARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood");
		var eowyn = scn.GetFreepsCard("eowyn");
		var merry = scn.GetFreepsCard("merry");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(eowyn, merry);
		// Beside Eowyn he is a [rohan] Man and may bear the mount.
		scn.AttachCardsTo(merry, mount);
		scn.MoveCardsToSupportArea(blood);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troop2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eowyn, troop}, new PhysicalCardImpl[]{merry, troop2});

		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(blood));

		// Eowyn, 6 against 13, is overwhelmed, and with her gone Merry is a Hobbit and no Man.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, eowyn);

		scn.FreepsResolveSkirmish(merry);
		assertAttachedTo(mount, merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(blood));
	}

	@Test
	public void TheProtectionEndsWithTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(guard, guard2, aragorn);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToSupportArea(blood);

		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.MoveMinionsToTable(rider);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, rider);
		scn.FreepsResolveSkirmish(guard);

		// 8 + 4 companions: double the Guard's 6.
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(12, scn.GetStrength(rider));
		scn.FreepsUseCardAction(blood);
		assertInZone(Zone.DISCARD, mount);
		scn.ShadowPass();
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.FREE_CHARACTERS, guard);

		// The fierce round: the same Guard against the same Rider, and no mount left to discard.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, rider);
		scn.FreepsResolveSkirmish(guard);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(12, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(blood));
		scn.PassFierceSkirmishActions();

		// Still protected he would have a second wound and be alive.
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
