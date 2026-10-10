package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
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

					// Two ranged weapons an Elf companion may bear.  Elven Bow makes its bearer an archer and nothing
					// else; Naith Longbow is strength +1 and makes nobody an archer, and its archery ability is
					// never used.  Both are put on by the rig.
					put("elvenbow", "1_41");
					put("longbow", "6_21");
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
	public void IsNotOfferedWithOneValiantElfAnElfWhoIsNotValiantAndAValiantMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var defender = scn.GetFreepsCard("defender");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(defender, lorienelf, guard);
		scn.MoveCardsToHand(leithio);

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
		var longbow = scn.GetFreepsCard("longbow");
		var sword = scn.GetFreepsCard("sword");
		var guard = scn.GetFreepsCard("guard");
		var rohirrimbow = scn.GetFreepsCard("rohirrimbow");
		scn.MoveCompanionsToTable(defender, balglin, lorienelf, guard);
		// One valiant Elf has a ranged weapon; the other has a weapon that is not ranged.  The other ranged
		// weapons on the table are borne by an Elf who is not valiant and by a valiant Man.
		scn.AttachCardsTo(defender, elvenbow);
		scn.AttachCardsTo(balglin, sword);
		scn.AttachCardsTo(lorienelf, longbow);
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
		var longbow = scn.GetFreepsCard("longbow");
		scn.MoveCompanionsToTable(defender, balglin);
		// Two ranged weapons that are not the same card: an Elven Bow and a Naith Longbow.
		scn.AttachCardsTo(defender, elvenbow);
		scn.AttachCardsTo(balglin, longbow);
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
	public void WithThreeValiantElvesNobodyIsAskedWhichToSpotAndTheTwoWithRangedWeaponsWoundTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var guardian = scn.GetFreepsCard("guardian");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var longbow = scn.GetFreepsCard("longbow");
		scn.MoveCompanionsToTable(defender, balglin, guardian);
		// Three valiant Elves: two bear a ranged weapon and the Lorien Guardian bears none.
		scn.AttachCardsTo(defender, elvenbow);
		scn.AttachCardsTo(balglin, longbow);
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

		// A branch in parentheses is mandatory when it is available: two valiant Elves bearing ranged weapons
		// can be spotted, so those are the two.  Nobody is asked which Elves to spot, and the first and only
		// thing asked is which minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(defender, balglin, guardian));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(savage);

		// Twice, although one valiant Elf in play has no ranged weapon.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.DISCARD, leithio);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
