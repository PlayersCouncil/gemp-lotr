package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "8_89");
					put("fury2", "8_89");
					put("fury3", "8_89");
					// Éowyn, Daughter of Éomund: [rohan] Man companion, strength 6, vitality 3; a maneuver ability used
					// only on purpose
					put("eowyn", "5_122");
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions or Éowyn can be
					// spotted; his trigger answers a [rohan] Man exerting, which nobody does here
					put("merry", "103_85");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant is its whole text
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text
					// Firefoot, Mearas of The Mark: [rohan] mount on a [rohan] Man, strength +1; its triggers need a
					// [rohan] token or Éomer
					put("firefoot", "13_126");
					// Knight's Mount: [gondor] mount on a knight, strength +1; an optional wound when its bearer wins
					put("kmount", "8_40");
					put("rshield", "5_91");     // Rohirrim Shield: a [rohan] possession on a [rohan] Man that is no mount; a regroup ability only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("savage2", "1_151");
					put("cguard", "10_81");     // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: the Rohirrim Herdsman's home is site 3, Barrows of Edoras 4_337 (a sanctuary), where he can be
	// assigned to a skirmish.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "8_89");
					put("fury2", "8_89");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant is its whole text
					// Rohirrim Herdsman: a [rohan] Man ally, home 3T, strength 4, vitality 2; a regroup ability used only
					// on purpose
					put("herdsman", "7_247");
					// Firefoot, Mearas of The Mark: [rohan] mount on a [rohan] Man, strength +1; its triggers need a
					// [rohan] token or Éomer
					put("firefoot", "13_126");
					// Snowmane, Noble Mearas: [rohan] mount on a [rohan] Man, strength +1; its triggers need a [rohan]
					// token or Théoden
					put("snowmane", "13_136");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger does not fire when cheated in
					put("cguard", "10_81");     // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; damage +1 only
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FuryoftheNorthmenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Fury of the Northmen
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a minion skirmishing a mounted [rohan] Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury");

		assertEquals("Fury of the Northmen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAMinionSkirmishingAMountedRohanManCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var fury = scn.GetFreepsCard("fury");
		var fury2 = scn.GetFreepsCard("fury2");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var firefoot = scn.GetFreepsCard("firefoot");
		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, firefoot);
		scn.AttachCardsTo(herdsman, snowmane);
		scn.MoveCardsToHand(fury, fury2);
		assertAttachedTo(firefoot, guard);
		assertAttachedTo(snowmane, herdsman);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var cguard = scn.GetShadowCard("cguard");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop, savage, runner, cguard);

		// The Free Peoples player's assignment actions, the phase before the skirmishes, at site 3.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(3, (int) scn.GetCurrentSite().getSiteNumber());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fury));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, savage},
				new PhysicalCardImpl[]{herdsman, cguard});
		scn.ShadowAssignToMinions(guard, troop, runner);

		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(guard, troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(cguard));
		assertTrue(scn.FreepsPlayAvailable(fury));
		scn.FreepsPlayCard(fury);

		// Minions of two cultures and races skirmishing the mounted Guard; not the Runner beside them, exhausted
		// (vitality 1), nor the Cirith Ungol Guard assigned to the mounted Herdsman, whose skirmish is to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, cguard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(cguard));
		assertInDiscard(fury);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// The ally's skirmish: a mounted [rohan] Man who is no companion.
		scn.FreepsResolveSkirmish(herdsman);
		assertTrue(scn.IsCharSkirmishingAgainst(herdsman, cguard));
		assertEquals(0, scn.GetWoundsOn(cguard));
		scn.FreepsPlayCard(fury2);

		// The one minion skirmishing him is exerted unasked.
		assertEquals(1, scn.GetWoundsOn(cguard));
		assertInDiscard(fury2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NoMinionIsExertedSkirmishingAnUnmountedOrNonRohanOrNonManCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var fury2 = scn.GetFreepsCard("fury2");
		var fury3 = scn.GetFreepsCard("fury3");
		var eowyn = scn.GetFreepsCard("eowyn");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var firefoot = scn.GetFreepsCard("firefoot");
		var kmount = scn.GetFreepsCard("kmount");
		var rshield = scn.GetFreepsCard("rshield");
		scn.MoveCompanionsToTable(eowyn, merry, guard, knight);
		// Beside Éowyn, Merry is a [rohan] Man and may bear the mount.
		scn.AttachCardsTo(merry, firefoot);
		scn.AttachCardsTo(knight, kmount);
		scn.AttachCardsTo(guard, rshield);
		scn.MoveCardsToHand(fury, fury2, fury3);
		assertAttachedTo(firefoot, merry);
		assertAttachedTo(kmount, knight);
		assertAttachedTo(rshield, guard);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var cguard = scn.GetShadowCard("cguard");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, cguard, savage2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eowyn, troop},
				new PhysicalCardImpl[]{merry, savage},
				new PhysicalCardImpl[]{guard, cguard},
				new PhysicalCardImpl[]{knight, savage2});

		// Éowyn, 6 against 13, is overwhelmed; with her gone Merry is a Hobbit and no Man, and two [rohan]
		// companions are left.
		scn.FreepsResolveSkirmish(eowyn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, eowyn);

		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage));
		assertAttachedTo(firefoot, merry);
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(0, scn.GetWoundsOn(savage));
		// An event's target is no requirement: it is played, and the mounted [rohan] Hobbit's minion is not exerted.
		assertTrue(scn.FreepsPlayAvailable(fury));
		scn.FreepsPlayCard(fury);
		assertInDiscard(fury);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// A [rohan] Man whose possession is no mount.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, cguard));
		assertEquals(0, scn.GetWoundsOn(cguard));
		scn.FreepsPlayCard(fury2);
		assertInDiscard(fury2);
		assertEquals(0, scn.GetWoundsOn(cguard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// A mounted Man of [gondor].
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishingAgainst(knight, savage2));
		assertEquals(0, scn.GetWoundsOn(savage2));
		scn.FreepsPlayCard(fury3);
		assertInDiscard(fury3);
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
