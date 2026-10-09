package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_115_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("triumph", "68_115");

					put("sam", "1_311");          // Sam, Son of Hamfast: strength 3, vitality 4, Ring-bound
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: an ally whose home is site 3, strength 8
					// Mithril-coat, Dwarf-mail: "Each minion skirmishing bearer loses all damage bonuses."
					put("coat", "13_153");

					// Uruk-hai of three cultures. Damage +1 is the whole text of the first three.
					put("warrior", "4_165");      // Orthanc Warrior: [isengard], strength 7, vitality 2
					put("savage", "1_151");       // Uruk Savage: [isengard], strength 5, vitality 3
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron], strength 9, vitality 2
					// Uruk-hai Troop: [uruk-hai], strength 9, vitality 4, damage +1; characters skirmishing it lose
					// strength bonuses from weapons, and nobody here bears one
					put("troop", "12_157");
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard, strength 8; his skirmish
					// ability is never used here
					put("saruman", "17_116");

					// For a companion of 6 vitality: each gives its bearer vitality +1, and their skirmish abilities
					// are never used here.
					put("saga", "1_114");         // The Saga of Elendil: a condition a [gondor] companion bears
					put("rangercloak", "15_68");  // Ranger's Cloak: a possession a [gondor] Man bears
					put("soldier", "1_271");      // Orc Soldier: a [sauron] Orc, strength 7, vitality 2; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FinalTriumphStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Final Triumph
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot your Uruk-hai skirmishing a companion to use vitality to resolve that skirmish instead of
		 * 	strength.  Characters in this skirmish cannot be overwhelmed.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("triumph");

		assertEquals("Final Triumph", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionOfTwiceTheVitalityBeatsAnUrukHaiOfTwiceHisStrengthAndNeitherIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var triumph = scn.GetShadowCard("triumph");
		var sam = scn.GetFreepsCard("sam");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(triumph);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, warrior);

		// On strength, 7 against 3 would overwhelm Sam. On vitality it is 4 against 2.
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(7, scn.GetStrength(warrior));
		assertEquals(4, scn.GetVitality(sam));
		assertEquals(2, scn.GetVitality(warrior));
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(triumph));
		scn.ShadowPlayCard(triumph);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, triumph);

		scn.PassSkirmishActions();
		// Sam wins. The Orthanc Warrior, at half Sam's vitality, is not overwhelmed: it takes the one wound.
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionAtHalfTheVitalityOfTheUrukHaiLosesAndIsNotOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var triumph = scn.GetShadowCard("triumph");
		var sam = scn.GetFreepsCard("sam");
		var coat = scn.GetFreepsCard("coat");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, coat);
		scn.MoveCardsToHand(triumph);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 2);
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, troop);

		// The Uruk-hai Troop has twice Sam's vitality, and three times his strength.
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(2, scn.GetVitality(sam));
		assertEquals(4, scn.GetVitality(troop));
		scn.SetTwilight(4);
		scn.FreepsPass();

		scn.ShadowPlayCard(triumph);
		scn.PassSkirmishActions();

		// Sam loses and is not overwhelmed. He takes one wound, the Mithril-coat having taken the Troop's
		// damage +1 away, and lives with three.
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionOfGreaterStrengthButLessVitalityLosesToTheUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var triumph = scn.GetShadowCard("triumph");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(triumph);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		// Aragorn's 8 is more than the Uruk-hai Troop's vitality of 4, but his own vitality is 3: it is the vitality
		// of each side that counts, and his strength counts for nothing.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetVitality(aragorn));
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(4, scn.GetVitality(troop));
		scn.SetTwilight(4);
		scn.FreepsPass();

		scn.ShadowPlayCard(triumph);
		scn.PassSkirmishActions();

		// The Troop wins, 4 to 3. Its damage +1 gives Aragorn a second wound, to three in all; the Troop has none.
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnOrcBesideTheUrukHaiInTheSkirmishIsNotOverwhelmedEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var triumph = scn.GetShadowCard("triumph");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var rangercloak = scn.GetFreepsCard("rangercloak");
		var warrior = scn.GetShadowCard("warrior");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga, rangercloak);
		scn.MoveCardsToHand(triumph);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, soldier);
		scn.AddWoundsToChar(warrior, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, warrior);
		scn.ShadowAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's 8 against 7 + 7. Vitality: 4 + 1 + 1 against the wounded Warrior's 1 and the Orc Soldier's 2,
		// which is double.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetVitality(aragorn));
		assertEquals(1, scn.GetVitality(warrior));
		assertEquals(2, scn.GetVitality(soldier));
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(triumph));
		scn.ShadowPlayCard(triumph);
		scn.PassSkirmishActions();

		// Aragorn wins and each minion takes a wound. The Warrior had 1 vitality left and dies of it; the Orc
		// Soldier, no Uruk-hai, is not overwhelmed and is in play with its one wound.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, warrior);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheNextSkirmishOfTheTurnIsResolvedOnStrengthAndACompanionInItIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var triumph = scn.GetShadowCard("triumph");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetShadowCard("guard");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(triumph);

		scn.StartGame();
		scn.MoveMinionsToTable(guard, warrior);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, guard}, new PhysicalCardImpl[]{sam, warrior});
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's 8 against the Cirith Ungol Guard's 9; vitality 4 against 2.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(guard));
		scn.SetTwilight(4);
		scn.FreepsPass();
		scn.ShadowPlayCard(triumph);
		scn.PassSkirmishActions();

		// Aragorn wins on vitality, and the Guard at half his vitality is not overwhelmed.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, guard);
		assertEquals(1, scn.GetWoundsOn(guard));

		// Sam's skirmish against the Orthanc Warrior is another skirmish: 3 against 7, vitality 4 against 2.
		// On vitality Sam would win, as he does when the event is played in his skirmish.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(4, scn.GetVitality(sam));
		assertEquals(2, scn.GetVitality(warrior));
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, sam);
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWhileAWizardOfTheUrukHaiCultureSkirmishesAndAnUrukHaiWaitsForItsOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var triumph = scn.GetShadowCard("triumph");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(triumph);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, saruman}, new PhysicalCardImpl[]{gimli, savage});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// Saruman is an [uruk-hai] minion and no Uruk-hai, and the Uruk Savage is assigned to Gimli, whose
		// skirmish has not begun.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(triumph));

		// The Free Peoples player has passed already.
		scn.ShadowPass();

		// In Gimli's skirmish against the Uruk Savage it can be played.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(triumph));

		scn.ShadowPlayCard(triumph);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.DISCARD, triumph);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWhileAnUrukHaiSkirmishesAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var triumph = scn.GetShadowCard("triumph");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(triumph);
		scn.MoveMinionsToTable(savage);

		// The fellowship moves to site 3, Elrond's home, where he may be assigned.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignAndResolve(elrond, savage);
		scn.SetTwilight(4);
		scn.FreepsPass();

		// An Uruk-hai of the Shadow player's is skirmishing, but not a companion.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(triumph));

		// The Free Peoples player has passed already.
		scn.ShadowPass();
		// Elrond's 8 against the Savage's 5, on strength.
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.HAND, triumph);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void StrengthGainedBeforeAndAfterItChangesNothingAndTheVitalitySpentOnItLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var triumph = scn.GetShadowCard("triumph");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(triumph);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// The skirmish is at Ettenmoors: "Skirmish: Exert your companion or minion to make that character
		// strength +2."
		scn.SkipToAssignments();
		var ettenmoors = scn.GetCurrentSite();
		assertEquals("Ettenmoors", ettenmoors.getBlueprint().getTitle());
		scn.FreepsAssignAndResolve(aragorn, troop);
		scn.SetTwilight(4);

		// Before the event is played, Aragorn exerts for strength: 10 against the Uruk-hai Troop's 9, with 3
		// vitality left against its 4.
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetVitality(aragorn));
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(4, scn.GetVitality(troop));

		scn.ShadowPlayCard(triumph);
		scn.FreepsPass();

		// After it, the Troop exerts twice for strength: 13 against 10, with 2 vitality left against 3.
		scn.ShadowUseCardAction(ettenmoors);
		scn.FreepsPass();
		scn.ShadowUseCardAction(ettenmoors);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(2, scn.GetVitality(troop));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetVitality(aragorn));

		scn.PassSkirmishActions();
		// Nobody's strength counts, gained before the event or after it. The Troop had the more vitality when
		// the event was played and has the less now, so Aragorn wins: the Troop takes a third wound, and
		// Aragorn, who would have taken two from a damage +1 minion, has only his own exertion.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(3, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
