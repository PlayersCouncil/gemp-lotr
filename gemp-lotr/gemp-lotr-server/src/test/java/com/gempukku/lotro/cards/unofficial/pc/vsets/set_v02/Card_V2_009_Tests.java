package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_009_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leithio", "102_9");

					// Valiant Elves.  None of their texts does anything in the archery phase.
					put("defender", "68_9");     // Elven Defender: not unique; stronger only beside an [elven] token
					put("balglin", "55_10");     // Balglin, Elven Warrior: unique; to play, spot an Elf; cost 2
					put("guardian", "54_76");    // Lorien Guardian: not unique; a regroup ability
					// Lorien Elf: an Elf companion who is not valiant, and has no text once in play.
					put("lorienelf", "1_53");
					// Household Guard: a [rohan] Man whose whole text is "Valiant".
					put("guard", "5_83");

					// Ranged weapons of two cultures.  Elven Bow makes its bearer an archer and nothing else;
					// Gondor Bow has a skirmish ability and a bonus for knights.  Both are put on by the rig.
					put("elvenbow", "1_41");
					put("gondorbow", "7_94");
					// Rohirrim Bow: a third ranged weapon, for a [rohan] Man.  Its archery ability is never used.
					put("rohirrimbow", "5_88");
					// Elven Sword: a hand weapon that is not a ranged weapon.  A skirmish ability.
					put("sword", "4_64");

					// Two minions of 3 vitality, of different cultures and races.
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, no text
					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LeithioiPhillinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Leithio i Phillin!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot 2 valiant Elves to wound a minion (or wound a minion twice if each of those Elves bears
		 * a ranged weapon).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leithio");

		assertEquals("Leithio i Phillin!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotOfferedWithOneValiantElfAnElfWhoIsNotValiantAndAValiantManOfTheElvenCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var defender = scn.GetFreepsCard("defender");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(defender, lorienelf, guard);
		scn.MoveCardsToHand(leithio);

		// Every valiant [elven] companion there is is an Elf, so the Household Guard is made [elven] besides
		// [rohan]: valiant and of the culture, and still a Man.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, guard, null, Culture.ELVEN));

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"), scn.GetShadowCard("savage"));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.HasKeyword(defender, Keyword.VALIANT));
		assertTrue(scn.IsRace(defender, Race.ELF));
		assertFalse(scn.HasKeyword(lorienelf, Keyword.VALIANT));
		assertTrue(scn.IsRace(lorienelf, Race.ELF));
		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		assertFalse(scn.IsRace(guard, Race.ELF));

		// One valiant Elf, with an Elf and a valiant companion beside him: one short.
		assertInZone(Zone.HAND, leithio);
		assertFalse(scn.FreepsPlayAvailable(leithio));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void SpotsTwoValiantElvesToWoundAMinionOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var leithio = scn.GetFreepsCard("leithio");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		scn.MoveCompanionsToTable(defender);
		scn.MoveCardsToHand(leithio, balglin);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		// The second valiant Elf arrives by legal play, and makes exactly two.
		scn.FreepsPlayCard(balglin);
		assertInZone(Zone.FREE_CHARACTERS, balglin);

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		int twilight = scn.GetTwilight();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(leithio));

		scn.FreepsPlayCard(leithio);

		// Either minion, and nobody else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(defender, balglin, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// Neither Elf bears a ranged weapon: one wound.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(balglin));
		assertInZone(Zone.DISCARD, leithio);
		assertEquals(0, scn.GetFreepsHandCount());
		// The event costs nothing.
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WoundsOnceWhenOnlyOneOfTheTwoValiantElvesBearsARangedWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var gondorbow = scn.GetFreepsCard("gondorbow");
		var sword = scn.GetFreepsCard("sword");
		var guard = scn.GetFreepsCard("guard");
		var rohirrimbow = scn.GetFreepsCard("rohirrimbow");
		scn.MoveCompanionsToTable(defender, balglin, lorienelf, guard);
		// One valiant Elf has a ranged weapon; the other has a weapon that is not ranged.  The other ranged
		// weapons on the table are borne by an Elf who is not valiant and by a valiant Man.
		scn.AttachCardsTo(defender, elvenbow);
		scn.AttachCardsTo(balglin, sword);
		scn.AttachCardsTo(lorienelf, gondorbow);
		scn.AttachCardsTo(guard, rohirrimbow);
		scn.MoveCardsToHand(leithio);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(leithio));

		scn.FreepsPlayCard(leithio);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, leithio);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WoundsOneMinionTwiceWhenEachOfTheTwoValiantElvesBearsARangedWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var gondorbow = scn.GetFreepsCard("gondorbow");
		scn.MoveCompanionsToTable(defender, balglin);
		// An [elven] ranged weapon and a [gondor] one.
		scn.AttachCardsTo(defender, elvenbow);
		scn.AttachCardsTo(balglin, gondorbow);
		scn.MoveCardsToHand(leithio);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(leithio));

		scn.FreepsPlayCard(leithio);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// One minion is chosen once and takes both wounds; nothing is asked a second time.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, leithio);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithThreeValiantElvesThePlayerChoosesTheTwoToSpotAndTheTwoWithRangedWeaponsWoundTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var guardian = scn.GetFreepsCard("guardian");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var guard = scn.GetFreepsCard("guard");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var gondorbow = scn.GetFreepsCard("gondorbow");
		scn.MoveCompanionsToTable(defender, balglin, guardian, lorienelf, guard);
		scn.AttachCardsTo(defender, elvenbow);
		scn.AttachCardsTo(balglin, gondorbow);
		scn.MoveCardsToHand(leithio);

		// A valiant Man of the [elven] culture, as in the test of the event not being offered.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, guard, null, Culture.ELVEN));

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(leithio));

		scn.FreepsPlayCard(leithio);

		// A player need not spot every card that meets a requirement, so with three valiant Elves in play he
		// is asked which two.  The Elf who is not valiant and the valiant Man are not among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(defender, balglin, guardian));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorienelf, guard));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		// The two with ranged weapons; the Lorien Guardian, who has none, is left out of it.
		scn.FreepsChooseCards(defender, balglin);

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(savage);

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	@Ignore("F-0047: the Elves chosen for the spot are not the ones whose ranged weapons are counted; spotting one without a ranged weapon still wounds twice")
	public void SpottingAValiantElfWithNoRangedWeaponWoundsOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var guardian = scn.GetFreepsCard("guardian");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var gondorbow = scn.GetFreepsCard("gondorbow");
		scn.MoveCompanionsToTable(defender, balglin, guardian);
		scn.AttachCardsTo(defender, elvenbow);
		scn.AttachCardsTo(balglin, gondorbow);
		scn.MoveCardsToHand(leithio);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsPlayCard(leithio);
		assertTrue(scn.FreepsHasCardChoiceAvailable(defender, balglin, guardian));
		// "Those Elves" are the two spotted.  One of these two bears a ranged weapon and the other does not,
		// whatever the Elf who was not spotted is carrying.
		scn.FreepsChooseCards(defender, guardian);

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AValiantElfOfAnotherCultureIsSpottedAndHisRangedWeaponCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var defender = scn.GetFreepsCard("defender");
		var guard = scn.GetFreepsCard("guard");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var gondorbow = scn.GetFreepsCard("gondorbow");
		scn.MoveCompanionsToTable(defender, guard);
		scn.AttachCardsTo(defender, elvenbow);
		scn.AttachCardsTo(guard, gondorbow);
		scn.MoveCardsToHand(leithio);

		// Every valiant Elf there is is [elven].  The text names the race and not the culture, so the Household
		// Guard is made an Elf besides a Man: a valiant Elf whose culture is [rohan].
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, guard, null, Race.ELF));

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.IsRace(guard, Race.ELF));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(leithio));

		scn.FreepsPlayCard(leithio);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(troop);

		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
