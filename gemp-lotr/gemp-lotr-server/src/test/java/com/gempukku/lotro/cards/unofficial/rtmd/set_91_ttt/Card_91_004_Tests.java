package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_004_Tests
{

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli, Son of Gloin: strength 6, vitality 3, Damage +1 of his own
					put("gimli", "1_13");
					// Gimli's Battle Axe: a possession, hand weapon, strength +2; "He is damage +1."
					put("axe", "1_14");
					// Lurtz, Now Perfected: strength 13, Damage +1 of his own
					put("lurtz", "15_162");
					// Lurtz's Sword: a Shadow possession, hand weapon, strength +2; "He is damage +1."
					put("sword", "2_43");

					// Aragorn, Ranger of the North: strength 8, no damage bonus of his own
					put("aragorn", "1_89");
					// Anduril, Sword That Was Broken: an ARTIFACT hand weapon, strength +2; "He is damage +1."
					put("anduril", "17_27");
					// Ring of Guile: an artifact that is no weapon (a ring), strength +1; "Bearer is damage +1."
					put("ring", "9_8");
					// Gondorian Knight: a [gondor] Man, strength 6, vitality 3, no other text
					put("knight", "5_35");
					// Gondorian Sword: hand weapon, strength +2.
					// "Skirmish: Discard 3 cards from hand to make bearer damage +1."
					put("gondorsword", "7_98");
					// Uruk Savage: strength 5, vitality 3
					put("savage", "1_151");
					// Axe Strike: three cards to discard from hand
					put("card1", "1_3");
					put("card2", "1_3");
					put("card3", "1_3");
					// Grimir, Dwarven Elder: a Dwarf ALLY, strength 3, vitality 3, damage of no kind
					put("grimir", "1_17");
					// Gimli's Battle Axe (Trusted Weapon): a possession, hand weapon, strength +2, bearer must be a
					// Dwarf; "While you can spot a threat, bearer is damage +1."
					put("dwarfaxe", "7_9");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_4", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli, Son of Gloin: strength 6, vitality 3, Damage +1 of his own
					put("gimli", "1_13");
					// Gimli's Battle Axe: a possession, hand weapon, strength +2; "He is damage +1."
					put("axe", "1_14");
					// Lurtz, Now Perfected: strength 13, Damage +1 of his own
					put("lurtz", "15_162");
					// Lurtz's Sword: a Shadow possession, hand weapon, strength +2; "He is damage +1."
					put("sword", "2_43");

					// Aragorn, Ranger of the North: strength 8, no damage bonus of his own
					put("aragorn", "1_89");
					// Anduril, Sword That Was Broken: an ARTIFACT hand weapon, strength +2; "He is damage +1."
					put("anduril", "17_27");
					// Ring of Guile: an artifact that is no weapon (a ring), strength +1; "Bearer is damage +1."
					put("ring", "9_8");
					// Gondorian Knight: a [gondor] Man, strength 6, vitality 3, no other text
					put("knight", "5_35");
					// Gondorian Sword: hand weapon, strength +2.
					// "Skirmish: Discard 3 cards from hand to make bearer damage +1."
					put("gondorsword", "7_98");
					// Uruk Savage: strength 5, vitality 3
					put("savage", "1_151");
					// Axe Strike: three cards to discard from hand
					put("card1", "1_3");
					put("card2", "1_3");
					put("card3", "1_3");
					// Grimir, Dwarven Elder: a Dwarf ALLY, strength 3, vitality 3, damage of no kind
					put("grimir", "1_17");
					// Gimli's Battle Axe (Trusted Weapon): a possession, hand weapon, strength +2, bearer must be a
					// Dwarf; "While you can spot a threat, bearer is damage +1."
					put("dwarfaxe", "7_9");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_4"
		);
	}

	@Test
	public void WeaponDamageLossStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_4
		 * Type: MetaSite
		 * Intensity: 1
		 * Game Text: Your weapons lose all damage bonuses.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_4", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getIntensity());
	}

	@Test
	public void YourWeaponsDamageIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_4: "Your weapons lose all damage bonuses."
		// FP's weapons lose damage, Shadow's weapons unaffected.

		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");

		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var lurtz = scn.GetShadowCard("lurtz");
		var sword = scn.GetShadowCard("sword");

		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);

		scn.StartGame();

		// Gimli has innate Damage +1, axe grants Damage +1, so normally Damage +2.
		// With the modifier, the axe's damage bonus should be cancelled.
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		//Lurtz unaffected
		assertEquals(2, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		// The axe has lost its damage bonus and nothing else: Gimli's 6 and its +2; Lurtz's 13 and his sword's +2
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(15, scn.GetStrength(lurtz));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnArtifactWeaponLosesItsDamageBonusAndARingKeepsIts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var gimli = scn.GetFreepsCard("gimli");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, anduril);
		scn.AttachCardsTo(gimli, ring);

		scn.StartGame();

		// Anduril is a weapon though no possession: Aragorn, with no damage bonus of his own, has none.
		// 8 + 2: the sword is his all the same
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertEquals(10, scn.GetStrength(aragorn));

		// The Ring of Guile gives its bearer damage +1 in the same words, and is no weapon: Gimli has his own
		// and the ring's.  6 + 1
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADamageBonusBoughtFromAWeaponsAbilityIsLostToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var knight = scn.GetFreepsCard("knight");
		var gondorSword = scn.GetFreepsCard("gondorsword");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, gondorSword);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.HasKeyword(knight, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(gondorSword));

		scn.FreepsUseCardAction(gondorSword);

		// The three cards are paid, and the sword has no damage bonus to give: "all damage bonuses".
		// With the bonus given the Knight would read damage +1.
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.HasKeyword(knight, Keyword.DAMAGE));
		// 6 + 2: the sword is still his
		assertEquals(8, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllysWeaponLosesItsDamageBonusToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var axe = scn.GetFreepsCard("dwarfaxe");
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(grimir, axe);

		scn.StartGame();

		// The axe gives damage +1 only while a threat can be spotted, and the modifier takes that from an
		// ally's weapon as from a companion's or a minion's
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.HasKeyword(grimir, Keyword.DAMAGE));
		// 3 + 2: the axe is still his
		assertEquals(5, scn.GetStrength(grimir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllysWeaponKeepsItsDamageBonusWhenTheOpponentHasTheModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var axe = scn.GetFreepsCard("dwarfaxe");
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(grimir, axe);

		scn.StartGame();

		// The same board as the test above, with the modifier on the other side of the table
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		// 3 + 2
		assertEquals(5, scn.GetStrength(grimir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OpponentWeaponsDamageIsNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_4: "Your weapons lose all damage bonuses."
		// When Shadow has this modifier, Freeps weapons should be unaffected.

		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");

		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var lurtz = scn.GetShadowCard("lurtz");
		var sword = scn.GetShadowCard("sword");

		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);

		scn.StartGame();

		// Shadow's modifier should not affect Freeps weapons
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		//Lurtz however is now affected
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADamageBonusBoughtFromAWeaponsAbilityIsKeptWhenTheOpponentHasTheModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var knight = scn.GetFreepsCard("knight");
		var gondorSword = scn.GetFreepsCard("gondorsword");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, gondorSword);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		// The board of the test above, with the modifier across the table: the sword's ability does give damage +1
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.HasKeyword(knight, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(gondorSword));

		scn.FreepsUseCardAction(gondorSword);

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		// 6 + 2
		assertEquals(8, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
