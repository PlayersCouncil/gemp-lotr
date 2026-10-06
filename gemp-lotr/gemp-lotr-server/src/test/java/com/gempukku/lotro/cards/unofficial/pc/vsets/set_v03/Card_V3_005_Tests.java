package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_005_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "103_5");
					put("gandalf", "1_364");        // Gandalf, The Grey Wizard: strength 7, vitality 4
					put("shirefolk", "1_72");       // Gandalf, Friend of the Shirefolk: a second Gandalf, only ever cheated in
					put("radagast", "9_26");        // Radagast, The Brown: a [gandalf] Wizard companion who is not Gandalf
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard", "5_83");           // Household Guard: a [rohan] Man, valiant and nothing else
					put("sleep", "1_84");           // Sleep, Caradhras: Fellowship event, exert Gandalf to discard every condition
					put("narya", "3_34");           // Narya: [gandalf] artifact (ring), vitality +1, bearer must be Gandalf
					put("staff", "2_22");           // Gandalf's Staff: [gandalf] artifact (staff), vitality +1, bearer must be Gandalf
					put("shards", "3_44");          // The Shards of Narsil: [gondor] artifact, in the support area
					put("horse", "4_283");          // Horse of Rohan: mount; bearer must be a Man, Elf or Wizard; to play, spot a [rohan] Man
					put("horse2", "4_283");

					put("troop1", "1_177");         // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("axe1", "1_190");           // Moria Axe: strength +2, bearer is damage +1, Skirmish: exert bearer to make him strength +2
					put("axe2", "1_190");
					put("drums", "1_168");          // Drums in the Deep: Shadow skirmish event, make a [moria] Orc strength +2
					put("party", "1_158");          // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1 printed
					put("lether", "7_63");          // Let Her Deal With Them: Shadow condition on a minion, bearer is damage +1
					put("inquisitor", "1_268");     // Orc Inquisitor: [sauron] Orc, strength 9, vitality 3; its text is for when it is played
					put("warg", "10_89");           // Gothmog's Warg: strength +1, vitality +1, on a [sauron] Orc
					put("captain", "10_75");        // Advance Captain: each roaming [sauron] minion is damage +1
					put("wall", "5_33");            // City Wall: Free Peoples condition that ends up on a minion; strength -3
					put("lemenya", "1_232");        // Ulaire Lemenya: Nazgul, strength 9, vitality 2
					put("fellbeast", "6_83");       // Fell Beast: strength +2, bearer is fierce, on a Nazgul
					put("explorer1", "4_250");      // Southron Explorer: [raider] Southron, strength 5, vitality 3; a regroup ability and nothing else
					put("explorer2", "4_250");
					put("horn", "103_54");          // Horn of Far Harad: on a Southron. Skirmish: remove a threat to make a Southron strength +2
					put("horn2", "103_54");
					put("sneak1", "17_58");         // Sunland Sneak: [men] minion, strength 14, vitality 3; its text only changes what it costs
					put("sneak2", "17_58");
					put("poleaxe", "11_95");        // Poleaxe: strength +2, on a [men] minion. Skirmish: remove (2) to make a [men] minion fierce until the regroup phase
					put("poleaxe2", "11_95");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowfaxStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Shadowfax, Swiftest of All
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be Gandalf.
		 * 	While you can spot a [gandalf] artifact, minions skirmishing bearer gain no strength or keyword bonuses
		 * 	from Shadow cards they bear.
		 * 	While bearer is unwounded or exhausted, bearer is strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadowfax");

		assertEquals("Shadowfax", card.getBlueprint().getTitle());
		assertEquals("Swiftest of All", card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysForTwoOnAGandalfAndOnNoOneElseAndMakesHimStrengthPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shirefolk = scn.GetFreepsCard("shirefolk");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		// Two Gandalfs can only share a table by cheating: Gandalf is unique.
		scn.MoveCompanionsToTable(gandalf, shirefolk, radagast, aragorn);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(gandalf));
		// 6 + 1 for the Hobbit (Frodo) + 1 for the Man (Aragorn)
		assertEquals(8, scn.GetStrength(shirefolk));
		assertTrue(scn.FreepsPlayAvailable(shadowfax));

		scn.FreepsPlayCard(shadowfax);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, shirefolk));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(radagast, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gandalf);
		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(shadowfax, gandalf);
		// 7 + 3 while unwounded
		assertEquals(10, scn.GetStrength(gandalf));
		// The other Gandalf, unwounded too, is not its bearer.
		assertEquals(8, scn.GetStrength(shirefolk));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("radagast"), scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(shadowfax));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlusThreeWhileUnwoundedOrExhaustedAndNotInBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("shadowfax"));
		scn.MoveCardsToHand(sleep);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(4, scn.GetVitality(gandalf));
		// 7 + 3
		assertEquals(10, scn.GetStrength(gandalf));
		// Aragorn is unwounded as well, and does not bear it.
		assertEquals(8, scn.GetStrength(aragorn));

		// Gandalf's first wound comes by legal play: the event's cost exerts him.
		scn.FreepsPlayCard(sleep);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(7, scn.GetStrength(gandalf));

		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(7, scn.GetStrength(gandalf));

		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAGandalfArtifactAMinionSkirmishingGandalfGainsNoStrengthOrKeywordFromTheShadowCardItBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narya = scn.GetFreepsCard("narya");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var axe1 = scn.GetShadowCard("axe1");
		var drums = scn.GetShadowCard("drums");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("shadowfax"));
		scn.MoveCardsToHand(narya);

		scn.StartGame();

		// The [gandalf] artifact arrives by legal play.
		scn.FreepsPlayCard(narya);
		assertAttachedTo(narya, gandalf);

		scn.MoveMinionsToTable(troop1, troop2);
		scn.AttachCardsTo(troop1, axe1);
		scn.AttachCardsTo(troop2, scn.GetShadowCard("axe2"));
		scn.MoveCardsToHand(drums);

		scn.SkipToAssignments();
		// 13 + 2 printed on the Axe
		assertEquals(15, scn.GetStrength(troop1));
		assertEquals(1, scn.GetKeywordCount(troop1, Keyword.DAMAGE));
		assertEquals(15, scn.GetStrength(troop2));
		assertEquals(1, scn.GetKeywordCount(troop2, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { gandalf, troop1 }, new PhysicalCardImpl[] { aragorn, troop2 });

		// Aragorn's skirmish first. The minion fighting Aragorn keeps its Axe's bonuses, and so does the one
		// assigned to Gandalf, whose skirmish has not begun.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(15, scn.GetStrength(troop2));
		assertEquals(1, scn.GetKeywordCount(troop2, Keyword.DAMAGE));
		assertEquals(15, scn.GetStrength(troop1));
		assertEquals(1, scn.GetKeywordCount(troop1, Keyword.DAMAGE));

		scn.PassCurrentPhaseActions();
		// 8 against 15: Aragorn loses and takes 1 wound + 1 for the Axe's damage +1
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(13, scn.GetStrength(troop1));
		assertFalse(scn.HasKeyword(troop1, Keyword.DAMAGE));
		assertEquals(15, scn.GetStrength(troop2));
		assertEquals(1, scn.GetKeywordCount(troop2, Keyword.DAMAGE));

		// The Axe's own ability is a Shadow card the minion bears as well: the cost is paid, the +2 is not gained.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(axe1));
		assertEquals(0, scn.GetWoundsOn(troop1));
		scn.ShadowUseCardAction(axe1);
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(13, scn.GetStrength(troop1));

		// A Shadow event is not a card the minion bears: its +2 is gained.
		scn.FreepsPass();
		scn.ShadowPlayCard(drums);
		scn.ShadowChooseCard(troop1);
		assertEquals(15, scn.GetStrength(troop1));

		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.PassCurrentPhaseActions();
		// 7 + 3 against 15: Gandalf loses and takes 1 wound, with no damage +1 to add another.
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// The skirmish over, the Axe's bonuses are back.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(15, scn.GetStrength(troop1));
		assertEquals(1, scn.GetKeywordCount(troop1, Keyword.DAMAGE));
	}

	@Test
	public void MinionsSkirmishingGandalfKeepWhatDoesNotComeFromAShadowCardTheyBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var party = scn.GetShadowCard("party");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var captain = scn.GetShadowCard("captain");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("shadowfax"));

		scn.StartGame();

		// Narya goes on after the start of the turn, which is when its own trigger would ask.
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("narya"));
		scn.MoveMinionsToTable(party, inquisitor, captain);
		// On the Uruk-hai: a Shadow condition that makes it damage +1 on top of the damage +1 it has printed,
		// and a Free Peoples condition that makes it strength -3.
		scn.AttachCardsTo(party, scn.GetShadowCard("lether"), scn.GetFreepsCard("wall"));
		// On the Orc: a mount that adds to its strength and to its vitality. The Advance Captain, which it does
		// not bear, makes it damage +1 while it roams.
		scn.AttachCardsTo(inquisitor, scn.GetShadowCard("warg"));

		scn.SkipToAssignments();
		// 9 - 3 for City Wall
		assertEquals(6, scn.GetStrength(party));
		assertEquals(2, scn.GetKeywordCount(party, Keyword.DAMAGE));
		// 9 + 1 printed on the Warg
		assertEquals(10, scn.GetStrength(inquisitor));
		// 3 + 1 printed on the Warg
		assertEquals(4, scn.GetVitality(inquisitor));
		assertEquals(1, scn.GetKeywordCount(inquisitor, Keyword.DAMAGE));

		// Both minions fight Gandalf at once; the Captain fights nobody.
		scn.FreepsAssignToMinions(gandalf, party);
		scn.ShadowAssignToMinions(gandalf, inquisitor);
		scn.FreepsResolveSkirmish(gandalf);

		// The Free Peoples card's -3 stays; the Shadow condition's damage +1 goes and the printed one stays.
		assertEquals(6, scn.GetStrength(party));
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		// The Warg's strength goes and its vitality stays; the Captain's damage +1 stays.
		assertEquals(9, scn.GetStrength(inquisitor));
		assertEquals(4, scn.GetVitality(inquisitor));
		assertEquals(1, scn.GetKeywordCount(inquisitor, Keyword.DAMAGE));

		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.PassCurrentPhaseActions();
		// 7 + 3 against 6 + 9: Gandalf loses and takes 1 wound + 1 for the printed damage +1 + 1 for the Captain's.
		assertEquals(3, scn.GetWoundsOn(gandalf));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(10, scn.GetStrength(inquisitor));
	}

	@Test
	public void BonusesGivenBetweenAMinionSkirmishingGandalfAndAMinionAssignedToSomeoneElseAreGainedBothWays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var sneak1 = scn.GetShadowCard("sneak1");
		var sneak2 = scn.GetShadowCard("sneak2");
		var horn1 = scn.GetShadowCard("horn");
		var horn2 = scn.GetShadowCard("horn2");
		var poleaxe1 = scn.GetShadowCard("poleaxe");
		var poleaxe2 = scn.GetShadowCard("poleaxe2");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("shadowfax"));

		scn.StartGame();

		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("narya"));
		scn.MoveMinionsToTable(explorer1, explorer2, sneak1, sneak2);
		// Each Southron bears a Horn and each [men] minion a Poleaxe. The Horns are cheated on, so their "when you
		// play this" has not hindered Shadowfax.
		scn.AttachCardsTo(explorer1, horn1);
		scn.AttachCardsTo(explorer2, horn2);
		scn.AttachCardsTo(sneak1, poleaxe1);
		scn.AttachCardsTo(sneak2, poleaxe2);
		scn.AddThreats(2);

		scn.SkipToAssignments();
		// 14 + 2 printed on the Poleaxe
		assertEquals(16, scn.GetStrength(sneak1));
		assertEquals(16, scn.GetStrength(sneak2));

		// One of each fights Gandalf; the other of each is assigned to Aragorn, whose skirmish waits.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { gandalf, explorer1 }, new PhysicalCardImpl[] { aragorn, explorer2 });
		scn.ShadowAssignToMinions(new PhysicalCardImpl[] { gandalf, sneak1 }, new PhysicalCardImpl[] { aragorn, sneak2 });
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(14, scn.GetStrength(sneak1));
		assertEquals(16, scn.GetStrength(sneak2));
		assertEquals(5, scn.GetStrength(explorer1));
		assertEquals(5, scn.GetStrength(explorer2));
		assertFalse(scn.HasKeyword(sneak1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sneak2, Keyword.FIERCE));
		assertEquals(2, scn.GetThreats());
		var twilight = scn.GetTwilight();

		// From a Horn whose bearer is skirmishing Gandalf, to a Southron who is not.
		scn.FreepsPass();
		scn.ShadowUseCardAction(horn1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer1, explorer2));
		scn.ShadowChooseCard(explorer2);
		// 5 + 2
		assertEquals(7, scn.GetStrength(explorer2));
		assertEquals(5, scn.GetStrength(explorer1));

		// From a Horn whose bearer is not skirmishing Gandalf, to the Southron who is: it is not a card he bears.
		scn.FreepsPass();
		scn.ShadowUseCardAction(horn2);
		scn.ShadowChooseCard(explorer1);
		assertEquals(7, scn.GetStrength(explorer1));
		assertEquals(0, scn.GetThreats());

		// The same two ways for a keyword, with the Poleaxes.
		scn.FreepsPass();
		scn.ShadowUseCardAction(poleaxe1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sneak1, sneak2));
		scn.ShadowChooseCard(sneak2);
		assertTrue(scn.HasKeyword(sneak2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sneak1, Keyword.FIERCE));

		scn.FreepsPass();
		scn.ShadowUseCardAction(poleaxe2);
		scn.ShadowChooseCard(sneak1);
		assertTrue(scn.HasKeyword(sneak1, Keyword.FIERCE));
		// (2) removed for each Poleaxe
		assertEquals(twilight - 4, scn.GetTwilight());
		// The Poleaxe he bears himself still gives him nothing.
		assertEquals(14, scn.GetStrength(sneak1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingGandalfGainsNothingFromAShadowCardThatAnotherMinionSkirmishingHimBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var sneak1 = scn.GetShadowCard("sneak1");
		var sneak2 = scn.GetShadowCard("sneak2");
		var horn = scn.GetShadowCard("horn");
		var poleaxe = scn.GetShadowCard("poleaxe");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("shadowfax"));

		scn.StartGame();

		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("narya"));
		scn.MoveMinionsToTable(explorer1, explorer2, sneak1, sneak2);
		// The Horn is cheated on, so its "when you play this" has not hindered Shadowfax.
		scn.AttachCardsTo(explorer1, horn);
		scn.AttachCardsTo(sneak1, poleaxe);
		scn.AddThreats(1);

		// All four fight Gandalf at once. Two of them bear nothing.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, explorer1);
		scn.ShadowAssignToMinions(gandalf, explorer2, sneak1, sneak2);
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(5, scn.GetStrength(explorer2));
		assertFalse(scn.HasKeyword(sneak2, Keyword.FIERCE));

		// "Shadow cards they bear" is read as the cards borne by any of the minions skirmishing Gandalf: the Horn
		// is on one of them, so the other Southron gains nothing from it. The threat is removed all the same.
		scn.FreepsPass();
		assertEquals(1, scn.GetThreats());
		scn.ShadowUseCardAction(horn);
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer1, explorer2));
		scn.ShadowChooseCard(explorer2);
		assertEquals(0, scn.GetThreats());
		assertEquals(5, scn.GetStrength(explorer2));

		// And the other [men] minion is not fierce in this skirmish for the Poleaxe, though the (2) is removed.
		scn.FreepsPass();
		var twilight = scn.GetTwilight();
		scn.ShadowUseCardAction(poleaxe);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sneak1, sneak2));
		scn.ShadowChooseCard(sneak2);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertFalse(scn.HasKeyword(sneak2, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnceNoGandalfArtifactCanBeSpottedAFierceMinionKeepsItsBonusesInItsSecondSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var staff = scn.GetFreepsCard("staff");
		var shards = scn.GetFreepsCard("shards");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax, staff);
		// An artifact of another culture, in play throughout.
		scn.MoveCardsToSupportArea(shards);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, scn.GetShadowCard("fellbeast"));

		scn.SkipToAssignments();
		// 9 + 2 printed on the Fell Beast
		assertEquals(11, scn.GetStrength(lemenya));
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));

		scn.FreepsAssignToMinions(gandalf, lemenya);
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(9, scn.GetStrength(lemenya));
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		// 7 + 3 against 9: Gandalf wins.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// The skirmish over, the minion is fierce again, so it is assigned a second time.
		assertEquals(11, scn.GetStrength(lemenya));
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));

		// The Staff is hindered, and a hindered card cannot be spotted as anything but a Free Peoples card. What
		// is left to spot is Shadowfax itself, a [gandalf] possession, and a [gondor] artifact.
		scn.HinderCard(staff);
		assertTrue(scn.IsHindered(staff));
		assertAttachedTo(staff, gandalf);
		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.SUPPORT, shards);

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, lemenya);
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(11, scn.GetStrength(lemenya));
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		// 7 + 3 against 11: Gandalf loses.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhileShadowfaxIsHinderedOnGandalfAHorseOfRohanStillCannotBePlayedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var horse = scn.GetFreepsCard("horse");
		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard");
		// The [rohan] Man the Horse needs spotted already has a mount, so Gandalf is the only one a Horse could go on.
		scn.MoveCompanionsToTable(gandalf, guard);
		scn.AttachCardsTo(guard, scn.GetFreepsCard("horse2"));
		scn.MoveCardsToHand(shadowfax, horse, narya);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(horse));

		scn.FreepsPlayCard(shadowfax);
		assertAttachedTo(shadowfax, gandalf);
		assertEquals(10, scn.GetStrength(gandalf));
		assertFalse(scn.FreepsPlayAvailable(horse));

		scn.HinderCard(shadowfax);
		// The game works out what may be played when it asks, so something is played to make it ask again.
		scn.FreepsPlayCard(narya);
		assertTrue(scn.IsHindered(shadowfax));
		// Hindered, its text is off...
		assertEquals(7, scn.GetStrength(gandalf));
		// ...and it is a mount on Gandalf all the same.
		assertFalse(scn.FreepsPlayAvailable(horse));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
